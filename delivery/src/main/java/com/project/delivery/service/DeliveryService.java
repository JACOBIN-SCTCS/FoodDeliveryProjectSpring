package com.project.delivery.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

import com.project.delivery.model.DeliveryAgent;
import com.project.delivery.model.Item;
import com.project.delivery.model.Order;
import com.project.delivery.model.OrderRequest;
import com.project.delivery.model.OrderStatus;
import com.project.delivery.model.WalletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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


    List<Item> itemList;
    HashMap<Long,Integer> agentStatus;
    PriorityQueue<Long> availableAgents;
    PriorityQueue<Long> pendingOrderList;
    HashMap<Long, Order> orderHistory; 


    long currentOrderId = INITIAL_ORDER_ID;

  
    // Parses data from initialData.txt file
    public void initializeData() throws Exception {

        // Reads initialData.txt file
        String userDirectory = new File("").getAbsolutePath();
        System.out.println(userDirectory);
        File file = new File(userDirectory+ "/initialData.txt");
        Scanner sc = new Scanner(file);
        
        itemList = new ArrayList<Item>();
        agentStatus = new HashMap<Long,Integer>();
        availableAgents = new PriorityQueue<Long>();
        pendingOrderList = new PriorityQueue<Long>();
        orderHistory = new HashMap<>();
        
        int count = 0;

        while (sc.hasNextLine()) {

            String str = sc.nextLine();
            System.out.println(str);
            String[] splited = str.split("\\s+");

            if (splited[0].indexOf('*') > -1) {
                count += 1;
                continue;
            }

            if (count == 0) {
                Long restId = Long.parseLong(splited[0]);
                int restNum = Integer.parseInt(splited[1]);

                for (int i = 0; i < restNum; i++) {

                    String str2 = sc.nextLine();
                    System.out.println(str2);
                    String[] splited2 = str2.split("\\s+");
                    
                    Long itemId, price, qty;

                    itemId = Long.parseLong(splited2[0]);
                    price  = Long.parseLong(splited2[1]);
                    qty    = Long.parseLong(splited2[2]);
                    
                    Item item = new Item(restId, itemId, price);
                    itemList.add(item);
                    
                    
                }
            
            }
            else if (count == 1) {
                agentStatus.put(Long.parseLong(str), SIGNED_OUT);
            }
            else if (count >= 2) {
                break;
            }
        }
        sc.close(); 
    }

    // Constructor for Delivery Service that initialises the In-Memory data structures
    public DeliveryService() {
        try {
            initializeData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    // Function that handles requestOrder endpoint
    public ResponseEntity<String> requestOrder(Long custId, Long restId, Long itemId, Long qty) {

        Long totalPrice = (long) 0;

        // Calculating total price for the given order
        for (Item item: itemList) {

            if (item.getRestId().equals(restId) && item.getItemId().equals(itemId)) {
                
                totalPrice = item.getPrice() * qty;
                break;

            } 
        }

        // Sending request to WALLET Service to Deduct order's price from the customer's balance
        WebClient client =  WebClient.create("http://host.docker.internal:8082");
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
        if(walletResponse==null)
        {
            return new ResponseEntity<String>("", HttpStatus.GONE);
        }
        System.out.println(walletResponse.getStatusCode());

        // If balance is deducted from customer's wallet successfully
        if (walletResponse.getStatusCode() == HttpStatus.CREATED) {

                System.out.println("Wallet amount deducted");
                
                // Sending request to RESTAURANT Service to check is order can be placed

                WebClient restaurantClient =  WebClient.create("http://host.docker.internal:8080");
                OrderRequest orderPayload = new OrderRequest(restId, itemId, qty)  ;  
                Mono<ResponseEntity<String>> restaurantReturnValue = restaurantClient.post()
                .uri("/acceptOrder")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(orderPayload), OrderRequest.class)
                .retrieve()
                .toEntity(String.class)
                .onErrorResume(WebClientResponseException.class,
                    ex -> ex.getRawStatusCode() == HttpStatus.GONE.value() ? Mono.empty() : Mono.empty());
    

                ResponseEntity<String> restaurantResponse = restaurantReturnValue .block();
                if(restaurantResponse==null)
                {
                    client =  WebClient.create("http://localhost:8082");
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
                System.out.println(restaurantResponse.getStatusCode());

                // If order gets accepted by the restaurant service successfully
                if (restaurantResponse.getStatusCode() == HttpStatus.CREATED) {

                    System.out.println("Order Accepted");

                    // If an Agent is available
                    if (availableAgents.size() > 0) {

                        // Assigns the agent with smallest id to the current order,
                        // Sets the order status to Assigned

                        long assignedAgent = availableAgents.poll();

                        Order currentOrder = new Order(custId, restId, itemId, qty);
                        currentOrder.setOrderId(currentOrderId);
                        currentOrder.setAgentId(assignedAgent);
                        currentOrder.setStatus(ORDER_ASSIGNED); 

                        // Records the order in the order history
                        orderHistory.put(currentOrderId, currentOrder);

                        // Sets the agent status to Unavailable
                        agentStatus.put(assignedAgent, UNAVAILABLE);


                    } else {

                        // Sets the order status to Unassigned

                        Order currentOrder = new Order(custId, restId, itemId, qty);
                        currentOrder.setOrderId(currentOrderId);
                        currentOrder.setStatus(ORDER_UNASSIGNED); 
                        currentOrder.setAgentId(-1l);

                        // Records the order in the order history
                        orderHistory.put(currentOrderId, currentOrder);

                        // Adds the current order to pending order list
                        pendingOrderList.add(currentOrderId);

                    }

                    // Returns order id with Http status 201
                    return new ResponseEntity<String>("{ \"orderId\": "+ String.valueOf(currentOrderId++) + "}", HttpStatus.CREATED); 

                } else {
                    
                    // Order is not accepted by restaurant service
                    // Restore order's price to Customer's wallet

                    client =  WebClient.create("http://host.docker.internal:8082");
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
    public Boolean agentSignIn(Long agentId) {

        // If agent id is not present or if signed out
        if (agentStatus.get(agentId) == null || agentStatus.get(agentId) == SIGNED_OUT) {

            // If unassigned orders are present
            if (pendingOrderList.size() > 0) {

                // Assign the agent to the order with smallest order id

                Order currentOrder = orderHistory.get(pendingOrderList.poll());

                currentOrder.setAgentId(agentId);
                currentOrder.setStatus(ORDER_ASSIGNED); 

                // Sets agent status to unavailable
                agentStatus.put(agentId, UNAVAILABLE);
                
            } else {

                // Sets agent status to available
                agentStatus.put(agentId, AVAILABLE);
                availableAgents.add(agentId);
            }            

        } 

        return true;

    }

    // Fuction that handles agentSignOut endpoint
    public Boolean agentSignOut(Long agentId) {

        // If agent status is available, then sets the status to signed out
        if (agentStatus.get(agentId) == AVAILABLE) {

            agentStatus.put(agentId, SIGNED_OUT);
            availableAgents.remove(agentId);
        }

        return true;
    }

    // Function that handles the orderDelivered endpoint
    public Boolean orderDelivered(Long orderId) {

        System.out.println("Order ID" + orderId );

        Order order  = orderHistory.getOrDefault(orderId,null);

        if(order==null || order.getStatus() != ORDER_ASSIGNED)  {
               System.out.println("Invalid order");
               return false;
        }

        System.out.println(order.getOrderId());

        order.setStatus(ORDER_DELIVERED);
        orderHistory.put(orderId, order);
        Long agentId = order.getAgentId();
        agentStatus.put(agentId, AVAILABLE);

        // If there are unassigned order, finds an agent for it
        if (pendingOrderList.size() > 0) {

            Order currentOrder = orderHistory.get(pendingOrderList.poll());

            currentOrder.setAgentId(agentId);
            currentOrder.setStatus(ORDER_ASSIGNED); 

            //orderHistory.put(currentOrderId, currentOrder);

            agentStatus.put(agentId, UNAVAILABLE);

            System.out.println("Agent" + agentId + "assigned to" + currentOrder.getOrderId());
            
        }

        if (agentStatus.get(agentId) == AVAILABLE) {
            availableAgents.add(agentId);
        }
        

        return true;
    }
    
    // Function that handles getorderStatus endpoint
    public ResponseEntity<OrderStatus> getOrderStatus(long orderId) {

        // If order id is not found in the order history
        if(!orderHistory.containsKey(orderId))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Order order = orderHistory.get(orderId);
        int orderstatus = order.getStatus();
        OrderStatus status = new OrderStatus(orderId);

        if(orderstatus==ORDER_UNASSIGNED) {
            status.setStatus(new String("unassigned"));
        }
        else if (orderstatus==ORDER_ASSIGNED) {
            status.setStatus(new String("assigned"));
        }
        else {
            status.setStatus(new String("delivered"));
        }

        status.setAgentId(order.getAgentId());

        // Returns status of the given order id
        return new ResponseEntity<OrderStatus>(status,HttpStatus.OK);
    }

    // Function that handles getAgentStatus endpoint
    public ResponseEntity<DeliveryAgent> getAgentStatus(long agentId) {

        int status = agentStatus.get(agentId);
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

    // Reinitializes the data in the Delivery service
    public void reInitialize() {

        // Clears all in-memory data structures (state)

        orderHistory.clear();
        agentStatus.replaceAll((K,V) -> V =SIGNED_OUT);
        pendingOrderList.clear();
        availableAgents.clear();
    }

}
