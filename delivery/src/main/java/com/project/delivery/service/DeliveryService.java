package com.project.delivery.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

import com.project.delivery.model.DeliveryAgent;
import com.project.delivery.model.Item;
import com.project.delivery.model.WalletRequest;
import com.project.delivery.model.OrderRequest;
import com.project.delivery.model.OrderStatus;
import com.project.delivery.model.Order;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class DeliveryService {

    List<Item> itemList;
    HashMap<Long,Integer> agentStatus;
    PriorityQueue<Long> availableAgents;
    PriorityQueue<Long> pendingOrderList;
    HashMap<Long, Order> orderHistory; 

    final int ORDER_UNASSIGNED = 0;
    final int ORDER_ASSIGNED = 1;
    final int ORDER_DELIVERED = 2;
    final int SIGNED_OUT = 0;
    final int AVAIALBLE = 1;
    final int UNAVAILABLE = 2;
    final long INITIAL_ORDER_ID = 1000;


    long currentOrderId = INITIAL_ORDER_ID;
  

    public void initialData() throws Exception {

        itemList = new ArrayList<Item>();

        String userDirectory = new File("").getAbsolutePath();
        System.out.println(userDirectory);
        File file = new File(userDirectory+ "/initialData.txt");
        Scanner sc = new Scanner(file);
        
        agentStatus = new HashMap<Long,Integer>();
        availableAgents = new PriorityQueue<Long>();
        pendingOrderList = new PriorityQueue<Long>();
        orderHistory = new HashMap<>();
        
        int count = 0;

        while (sc.hasNextLine()) {

            String str = sc.nextLine();
            System.out.println(str);
            String[] splited = str.split("\\s+");
            if (splited[0].indexOf('*') > -1) 
            {
                count+=1;
                continue;
            }
            if(count==0)
            {
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
            else if(count==1)
            {
                agentStatus.put(Long.parseLong(str), SIGNED_OUT);
            }
            else if(count>=2)
            {
                break;
            }
        }
        sc.close(); 
    }

    public DeliveryService() {

        try {
            initialData();
        } catch (Exception e) {
            
            e.printStackTrace();
        }
        
    }
    
    public ResponseEntity<String> requestOrder(Long custId, Long restId, Long itemId, Long qty) {

        Long totalPrice=(long)0;

        for (Item item: itemList) {

            if (item.getRestId().equals(restId) && item.getItemId().equals(itemId)) {
                
                totalPrice = item.getPrice() * qty;
                break;

            } 
        }

      WebClient client =  WebClient.create("http://localhost:8082");
      WalletRequest payload = new WalletRequest(custId, totalPrice)  ;  
      Mono<ResponseEntity<String>> retvalue = client.post()
      .uri("/deductBalance")
      .contentType(MediaType.APPLICATION_JSON)
      .body(Mono.just(payload), WalletRequest.class)
      .retrieve()
      .toEntity(String.class);
  

       ResponseEntity<String> walletResponse = retvalue.block();
       System.out.println(walletResponse.getStatusCode());

       if (walletResponse.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Wallet amount deducted");

            WebClient restaurantClient =  WebClient.create("http://localhost:8080");
            OrderRequest orderPayload = new OrderRequest(restId, itemId, qty)  ;  
            Mono<ResponseEntity<String>> restaurantReturnValue = restaurantClient.post()
            .uri("/acceptOrder")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(orderPayload), OrderRequest.class)
            .retrieve()
            .toEntity(String.class);
  

            ResponseEntity<String> restaurantResponse = restaurantReturnValue .block();
            System.out.println(restaurantResponse.getStatusCode());

            if (restaurantResponse.getStatusCode() == HttpStatus.CREATED) {
                System.out.println("Order Accepted");

                if (availableAgents.size() > 0) {

                    long assignedAgent = availableAgents.poll();

                    Order currentOrder = new Order(custId, restId, itemId, qty);
                    currentOrder.setOrderId(currentOrderId);
                    currentOrder.setAgentId(assignedAgent);
                    currentOrder.setStatus(ORDER_ASSIGNED); //assigned

                    orderHistory.put(currentOrderId, currentOrder);

                    //currentOrderId++;

                    agentStatus.put(assignedAgent, UNAVAILABLE);


                } else {

                    Order currentOrder = new Order(custId, restId, itemId, qty);
                    currentOrder.setOrderId(currentOrderId);
                    currentOrder.setStatus(ORDER_UNASSIGNED); //Unassigned
                    currentOrder.setAgentId(-1l);
                    orderHistory.put(currentOrderId, currentOrder);

                    pendingOrderList.add(currentOrderId);

                    //currentOrderId++;

                }

                return new ResponseEntity<String>("{ \"orderId\": "+ String.valueOf(currentOrderId++) + "}", HttpStatus.CREATED); //Return order id also

            } else {

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
       }

       else {
        return new ResponseEntity<String>("", HttpStatus.GONE);
       }
       
    }

    public Boolean agentSignIn(Long agentId) {

        if (agentStatus.get(agentId) == null || agentStatus.get(agentId) == SIGNED_OUT) {

            if (pendingOrderList.size() > 0) {

                Order currentOrder = orderHistory.get(pendingOrderList.poll());

                currentOrder.setAgentId(agentId);
                currentOrder.setStatus(ORDER_ASSIGNED); //assigned

                //orderHistory.put(currentOrderId, currentOrder);

                agentStatus.put(agentId, UNAVAILABLE);
                
            } else 
            {

                agentStatus.put(agentId, AVAIALBLE);
                availableAgents.add(agentId);
            }

            

        } 

        return true;
    }

    public Boolean agentSignOut(Long agentId) {

        if (agentStatus.get(agentId) == AVAIALBLE) {

            agentStatus.put(agentId, SIGNED_OUT);
            availableAgents.remove(agentId);
        }

        return true;
    }

    public Boolean orderDelivered(Long orderId)
    {
        System.out.println("Order ID" + orderId );
        Order order  = orderHistory.getOrDefault(orderId,null);
        if(order==null || order.getStatus() != ORDER_ASSIGNED)
        {
               System.out.println("Invalid order");
               return false;
        }
        System.out.println(order.getOrderId());
        order.setStatus(ORDER_DELIVERED);
        orderHistory.put(orderId, order);
        Long agentId = order.getAgentId();
        agentStatus.put(agentId, AVAIALBLE);

        if (pendingOrderList.size() > 0) {

            Order currentOrder = orderHistory.get(pendingOrderList.poll());

            currentOrder.setAgentId(agentId);
            currentOrder.setStatus(ORDER_ASSIGNED); //assigned

            //orderHistory.put(currentOrderId, currentOrder);

            agentStatus.put(agentId, UNAVAILABLE);
            System.out.println("Agent" + agentId + "assigned to" + currentOrder.getOrderId());
            
        }
        if(agentStatus.get(agentId)==AVAIALBLE)
        {
            availableAgents.add(agentId);
        }
        

        return true;
    }
    
    public ResponseEntity<OrderStatus> getOrderStatus(long orderId)
    {
        if(!orderHistory.containsKey(orderId))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Order order = orderHistory.get(orderId);
        int orderstatus = order.getStatus();
        OrderStatus status = new OrderStatus(orderId);
        if(orderstatus==ORDER_UNASSIGNED)
        {
            status.setStatus(new String("unassigned"));
            //return new ResponseEntity<String>("{ \"orderId\":" + String.valueOf(order.getOrderId()) + ", \"status\": \"unassigned\" , \"agentId\":" + String.valueOf(order.getAgentId()) + " }", HttpStatus.OK);

        }
        else if (orderstatus==ORDER_ASSIGNED)
        {
            status.setStatus(new String("assigned"));
            //return new ResponseEntity<String>("{ \"orderId\":" + String.valueOf(order.getOrderId()) + ", \"status\": \"assigned\" , \"agentId\":" + String.valueOf(order.getAgentId()) + " }", HttpStatus.OK);

        }
        else
        {
            status.setStatus(new String("delivered"));
            //return new ResponseEntity<String>("{ \"orderId\":" + String.valueOf(order.getOrderId()) + ", \"status\": \"delivered\" , \"agentId\":" + String.valueOf(order.getAgentId()) + " }", HttpStatus.OK);
        }
        status.setAgentId(order.getAgentId());
        return new ResponseEntity<OrderStatus>(status,HttpStatus.OK);
    }

    public ResponseEntity<DeliveryAgent> getAgentStatus(long agentId) {
        int status = agentStatus.get(agentId);
        DeliveryAgent agent = new DeliveryAgent(agentId);
        if (status == AVAIALBLE) {
            agent.setStatus(new String("available"));

        } else if (status == UNAVAILABLE) {
            agent.setStatus(new String("unavailable"));
            //return new ResponseEntity<String>("{ \"agentId\":" + String.valueOf(agentId) + ", \"status\": \"unavailable\"", HttpStatus.OK);
        } 
        else {
            agent.setStatus(new String("signed-out"));
            //return new ResponseEntity<String>("{ \"agentId\":" + String.valueOf(agentId) + ", \"status\": \"signed-out\"", HttpStatus.OK);
        }
        return new ResponseEntity<>(agent, HttpStatus.OK);
    }

    public void reInitialize()
    {
        orderHistory.clear();
        agentStatus.replaceAll((K,V) -> V =SIGNED_OUT);
        pendingOrderList.clear();
        availableAgents.clear();
    }


}
