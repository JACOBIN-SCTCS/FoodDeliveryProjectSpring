package com.project.delivery.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import com.project.delivery.entities.AgentEntity;
import com.project.delivery.entities.CurrentState;
import com.project.delivery.entities.OrderHistory;
import com.project.delivery.entities.RestaurantEntity;
import com.project.delivery.model.DeliveryAgent;
import com.project.delivery.model.Item;
import com.project.delivery.model.Order;
import com.project.delivery.model.OrderRequest;
import com.project.delivery.model.OrderStatus;
import com.project.delivery.model.WalletRequest;
import com.project.delivery.repositories.AgentsRepository;
import com.project.delivery.repositories.CurrentStateRepository;
import com.project.delivery.repositories.OrderHistoryRepository;
import com.project.delivery.repositories.RestaurantRepository;

import org.aspectj.weaver.loadtime.Agent;
import org.hibernate.LockMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import net.bytebuddy.asm.Advice.This;
import reactor.core.publisher.Mono;

// Provides functions that handles all the endpoints of Delivery service

@Component
public class DeliveryService {

    //Constants for Order Status
    final int ORDER_UNASSIGNED  = 0;
    final int ORDER_ASSIGNED    = 1;
    final int ORDER_DELIVERED   = 2;


    //Constants for Agent Status
    final int SIGNED_OUT    = 0;
    final int AVAILABLE     = 1;
    final int UNAVAILABLE   = 2;
    
    //Initial Order Id
    final long INITIAL_ORDER_ID = 1000;
    private String walleturl="http://kawinwallet:8080";
    private String restauranturl="http://kawinrestaurant:8080";

    @Autowired
    public AgentsRepository agentsRepository;

    @Autowired
    public RestaurantRepository restaurantRepository;

    @Autowired
    public OrderHistoryRepository orderHistoryRepository;

    @Autowired
    public CurrentStateRepository currentStateRepository;

    @Autowired
    public EntityManager em;

    long currentOrderId = INITIAL_ORDER_ID;

    // Constructor for Delivery Service that initialises the repositories for database tables
    public DeliveryService(AgentsRepository agentsRepository, RestaurantRepository restaurantRepository,
        OrderHistoryRepository orderHistoryRepository,
        CurrentStateRepository currentStateRepository
    ) {
        
        this.agentsRepository       = agentsRepository;
        this.restaurantRepository   = restaurantRepository;
        this.orderHistoryRepository = orderHistoryRepository;
        this.currentStateRepository = currentStateRepository;
        
    }
    
    // Function that handles requestOrder endpoint
    @Transactional
    public ResponseEntity<String> requestOrder(Long custId, Long restId, Long itemId, Long qty) {

        // Calculating total price for the given order

        // Gets global lock on all tables so that multiple tables aren't accessed at the same time
        CurrentState global_lock = this.em.find(CurrentState.class, 2,LockModeType.PESSIMISTIC_WRITE);

        // Queries for the price of the given item from the database
        RestaurantEntity current_item = (RestaurantEntity) this.em.createQuery("SELECT t FROM RestaurantEntity t WHERE t.itemId = :value1 AND t.restId = :value2")
                                                                .setParameter("value1", itemId)
                                                                .setParameter("value2", restId)
                                                                .getSingleResult();

        Long totalPrice = (long) current_item.getPrice() * qty;

        System.out.println("Total Price" + totalPrice);

        // Sending request to WALLET Service to Deduct order's price from the customer's balance
        WebClient client =  WebClient.create(walleturl);
        WalletRequest payload = new WalletRequest(custId, totalPrice);  

        Mono<ResponseEntity<String>> retvalue = client.post()
            .uri("/deductBalance")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(payload), WalletRequest.class)
            .retrieve()
            .toEntity(String.class)
            .onErrorResume(WebClientResponseException.class,
                ex -> ex.getRawStatusCode() ==  HttpStatus.GONE.value() ? Mono.empty() : Mono.empty());
    

        ResponseEntity<String> walletResponse = retvalue.block();
        
        if ( walletResponse == null ) {
            return new ResponseEntity<String>("", HttpStatus.GONE);
        }
        System.out.println(walletResponse.getStatusCode());

        // If balance is deducted from customer's wallet successfully
        if (walletResponse.getStatusCode() == HttpStatus.CREATED) {

                System.out.println("Wallet amount deducted");
                
                // Sending request to RESTAURANT Service to check is order can be placed

                WebClient restaurantClient =  WebClient.create(restauranturl);
                OrderRequest orderPayload = new OrderRequest(restId, itemId, qty);  

                Mono<ResponseEntity<String>> restaurantReturnValue = restaurantClient.post()
                    .uri("/acceptOrder")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(orderPayload), OrderRequest.class)
                    .retrieve()
                    .toEntity(String.class)
                    .onErrorResume(WebClientResponseException.class,
                        ex -> ex.getRawStatusCode() == HttpStatus.GONE.value() ? Mono.empty() : Mono.empty());
    

                ResponseEntity<String> restaurantResponse = restaurantReturnValue .block();

                if( restaurantResponse == null ) {
                    client =  WebClient.create(walleturl);
                    payload = new WalletRequest(custId, totalPrice);  

                    retvalue = client.post()
                        .uri("/addBalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(payload), WalletRequest.class)
                        .retrieve()
                        .toEntity(String.class);
                
                    walletResponse = retvalue.block();
                    System.out.println(walletResponse.getStatusCode());

                    return new ResponseEntity<String>("", HttpStatus.GONE);
                }
                System.out.println(restaurantResponse.getStatusCode());

                // If order gets accepted by the restaurant service successfully
                if (restaurantResponse.getStatusCode() == HttpStatus.CREATED) {

                    System.out.println("Order Accepted");

                    // Queries for the available agents ordered based on agent id from the database 
                    TypedQuery<AgentEntity> agent_query =  this.em.createQuery("SELECT t FROM AgentEntity t WHERE t.status = "+ AVAILABLE + " ORDER BY agentId ASC ", AgentEntity.class)
                                                                .setLockMode(LockModeType.PESSIMISTIC_WRITE);
                    
                    List<AgentEntity> agent_result = agent_query.getResultList();
                    AgentEntity unassigned_agent = agent_result.isEmpty() ? null : agent_result.get(0);
                    
                    // If an Agent is available
                    if (unassigned_agent != null) {

                        // Assigns the agent with smallest id to the current order,
                        // Sets the order status to Assigned
                        CurrentState orderIdState = em.find(CurrentState.class, 1,LockModeType.PESSIMISTIC_WRITE); 
                        OrderHistory currentOrder = new OrderHistory(orderIdState.getValue(), restId, custId, itemId, qty, unassigned_agent.getAgentId(), ORDER_ASSIGNED);
                        currentOrderId = orderIdState.getValue();
                        orderIdState.setValue(orderIdState.getValue()+1);
                        
                        // Records the order in the order history
                        this.em.persist(currentOrder);
                        
                        // Sets the agent status to Unavailable
                        unassigned_agent.setStatus(UNAVAILABLE);
                        this.em.merge(unassigned_agent);
                        this.em.merge(orderIdState);
                        this.em.flush();

                    } else {

                        // Sets the order status to unassigned 
                        CurrentState orderIdState = em.find(CurrentState.class, 1,LockModeType.PESSIMISTIC_WRITE); 
                        OrderHistory currentOrder = new OrderHistory(orderIdState.getValue(), restId, custId, itemId, qty, -1l, ORDER_UNASSIGNED);
                        currentOrderId = orderIdState.getValue();
                        orderIdState.setValue(orderIdState.getValue()+1);

                        // Records the order in the order history
                        this.em.merge(currentOrder);
                        this.em.merge(orderIdState);
                        this.em.flush();

                    }    

                    // Returns order id with Http status 201
                    return new ResponseEntity<String>("{ \"orderId\": "+ String.valueOf(currentOrderId++) + "}", HttpStatus.CREATED); 

                } else {
                    
                    // Order is not accepted by restaurant service
                    // Restore order's price to Customer's wallet

                    client =  WebClient.create(walleturl);
                    payload = new WalletRequest(custId, totalPrice)  ;  
                    retvalue = client.post()
                        .uri("/addBalance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(payload), WalletRequest.class)
                        .retrieve()
                        .toEntity(String.class);
                
                    walletResponse = retvalue.block();
                    
                    System.out.println(walletResponse.getStatusCode());

                    return new ResponseEntity<String>("", HttpStatus.GONE);

                }
        }
        else {
            return new ResponseEntity<String>("", HttpStatus.GONE);
        }
        
    }

    // Fuction that handles agentSignIn endpoint
    @Transactional
    public Boolean agentSignIn(Long agentId) {

        // Acquires global lock for all tables in the database
        CurrentState global_lock = this.em.find(CurrentState.class, 2,LockModeType.PESSIMISTIC_WRITE);

        // Finds the record for the given agent
        AgentEntity current_agent = this.em.find(AgentEntity.class, agentId, LockModeType.PESSIMISTIC_WRITE);

        // If agent id is not present or if signed out
        if (current_agent.getStatus() == SIGNED_OUT) {

            // Finds all the unassigned orders sorted based on their order id
            TypedQuery<OrderHistory> order_query =  this.em.createQuery("SELECT t FROM OrderHistory t WHERE t.assigned = "+ ORDER_UNASSIGNED + " ORDER BY orderId ASC ", OrderHistory.class)
                                                    .setMaxResults(1)                
                                                    .setLockMode(LockModeType.PESSIMISTIC_WRITE);
            
            
            List<OrderHistory> order_result = order_query.getResultList();
            OrderHistory unassigned_order = order_result.isEmpty() ? null : order_result.get(0);

            // If unassigned orders are present
            if (unassigned_order != null) {
                
                // Finds any available agent with smaller agent id
                TypedQuery<AgentEntity> agent_query =  this.em.createQuery("SELECT t FROM AgentEntity t WHERE t.status = "+ AVAILABLE + " ORDER BY agentId ASC ", AgentEntity.class)
                                                                    .setLockMode(LockModeType.PESSIMISTIC_WRITE);

                List<AgentEntity> agent_result = agent_query.getResultList();

                AgentEntity unassigned_agent = agent_result.isEmpty() ? null : agent_result.get(0);
                
                // Assign the agent to the order with smallest order id
                if (unassigned_agent == null || current_agent.getAgentId() < unassigned_agent.getAgentId()) {
                    
                    System.out.println("Agent null");

                    // Sets agent status to unavailable
                    current_agent.setStatus(UNAVAILABLE);

                    unassigned_order.setAgentId(current_agent.getAgentId());
                    unassigned_order.setAssigned(ORDER_ASSIGNED); 

                    this.em.merge(current_agent);
                    this.em.merge(unassigned_order);
                    this.em.flush();
                
                } else {

                    // Sets agent status to unavailable
                    AgentEntity assigning_agent = this.em.find(AgentEntity.class, unassigned_agent.getAgentId(), LockModeType.PESSIMISTIC_WRITE);
                    assigning_agent.setStatus(UNAVAILABLE);

                    unassigned_order.setAgentId(current_agent.getAgentId());
                    unassigned_order.setAssigned(ORDER_ASSIGNED); 

                    this.em.merge(assigning_agent);
                    this.em.merge(unassigned_order);
                    this.em.flush(); 
                    System.out.println("Agent id " + unassigned_agent.getAgentId() );
                }                     
                

            } else {

                // Sets agent status to available
                current_agent.setStatus(AVAILABLE);
                this.em.merge(current_agent);
                this.em.flush();
            }          

        } 

        return true;

    }

    // Fuction that handles agentSignOut endpoint
    @Transactional
    public Boolean agentSignOut(Long agentId) {

        CurrentState global_lock = this.em.find(CurrentState.class, 2,LockModeType.PESSIMISTIC_WRITE);
        AgentEntity current_agent = this.em.find(AgentEntity.class, agentId, LockModeType.PESSIMISTIC_WRITE);

        // If agent status is available, then sets the status to signed out
        if (current_agent.getStatus() == AVAILABLE) {

            current_agent.setStatus(SIGNED_OUT);
            this.em.merge(current_agent);
            this.em.flush();
        }

        return true;
    }

    // Function that handles the orderDelivered endpoint
    @Transactional
    public Boolean orderDelivered(Long orderId) {
        
        // Acquires global lock for all tables in the database
        CurrentState global_lock = this.em.find(CurrentState.class, 2,LockModeType.PESSIMISTIC_WRITE);
        System.out.println("Order ID" + orderId );

        // Finds the order with the given order id
        OrderHistory order = this.em.find(OrderHistory.class, orderId, LockModeType.PESSIMISTIC_WRITE);

        // Checks if the order id is present or if it is already delivered
        if (order == null || order.getAssigned() != ORDER_ASSIGNED)  {

            System.out.println("Invalid order");
            return false;
        }

        // Updates the order status to delivered
        order.setAssigned(ORDER_DELIVERED);
        this.em.merge(order);
        this.em.flush();

        Long agentId = order.getAgentId();

        // Queries for any unassigned orders 
        TypedQuery<OrderHistory> order_query =  this.em.createQuery("SELECT t FROM OrderHistory t WHERE t.assigned = "+ ORDER_UNASSIGNED + " ORDER BY orderId ASC ", OrderHistory.class)
                                                                  .setLockMode(LockModeType.PESSIMISTIC_WRITE);

        List<OrderHistory> order_result = order_query.getResultList();

        OrderHistory unassigned_order = order_result.isEmpty() ? null : order_result.get(0);

        // If there are unassigned order, finds an agent for it
        if (unassigned_order != null) {

            unassigned_order.setAgentId(agentId);
            unassigned_order.setAssigned(ORDER_ASSIGNED); 
            this.em.merge(unassigned_order);
            this.em.flush();

            System.out.println("Agent" + agentId + "assigned to" + unassigned_order.getOrderId());
            
        } else {

            // Updates the status of agent to available
            AgentEntity current_agent = this.em.find(AgentEntity.class, agentId, LockModeType.PESSIMISTIC_WRITE);

            current_agent.setStatus(AVAILABLE);
            this.em.merge(current_agent);
            this.em.flush();
        }

        return true;
    }
    
    // Function that handles getorderStatus endpoint
    @Transactional
    public ResponseEntity<OrderStatus> getOrderStatus(long orderId) {

        
        CurrentState global_lock = this.em.find(CurrentState.class, 2,LockModeType.PESSIMISTIC_WRITE);
        
        // Finds the record for the given order id
        OrderHistory hist = this.em.find(OrderHistory.class, orderId, LockModeType.PESSIMISTIC_READ);

        // If order id is not found in the order history
        if ( hist == null ) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        int orderstatus = hist.getAssigned();
        OrderStatus status = new OrderStatus(orderId);

        if ( orderstatus == ORDER_UNASSIGNED ) {
            status.setStatus(new String("unassigned"));
        }
        else if ( orderstatus == ORDER_ASSIGNED ) {
            status.setStatus(new String("assigned"));
        }
        else {
            status.setStatus(new String("delivered"));
        }

        status.setAgentId(hist.getAgentId());

        // Returns status of the given order id
        return new ResponseEntity<OrderStatus>(status,HttpStatus.OK);
    }

    // Function that handles getAgentStatus endpoint
    @Transactional
    public ResponseEntity<DeliveryAgent> getAgentStatus(long agentId) {

        AgentEntity agentstatus = this.em.find(AgentEntity.class, agentId, LockModeType.PESSIMISTIC_READ);  
        int status = agentstatus.getStatus();

        DeliveryAgent agent = new DeliveryAgent(agentId);
        
        if (status == AVAILABLE) {
            agent.setStatus(new String("available"));

        } else if (status == UNAVAILABLE) {
            agent.setStatus(new String("unavailable"));
        } 
        else {
            agent.setStatus(new String("signed-out"));
        }

        // Returns status of the given agent id
        return new ResponseEntity<>(agent, HttpStatus.OK);
    }

}
