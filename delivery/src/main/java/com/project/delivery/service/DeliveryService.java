package com.project.delivery.service;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

import javax.print.DocFlavor.STRING;

import com.project.delivery.model.Item;
import com.project.delivery.model.WalletRequest;
import com.project.delivery.model.OrderRequest;
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

    final int SIGNED_OUT = 0;
    final int AVAIALBLE = 1;
    final int UNAVAILABLE = 2;
    final long INITIAL_ORDER_ID = 1000;


    long currentOrderId = INITIAL_ORDER_ID;
  

    public void initialData() throws Exception {

        itemList = new ArrayList<Item>();

        String userDirectory = new File("").getAbsolutePath();
        System.out.println(userDirectory);
        File file = new File(userDirectory+ "/target/initialData.txt");
        Scanner sc = new Scanner(file);
        
        agentStatus = new HashMap<Long,Integer>();
        availableAgents = new PriorityQueue<Long>();
        pendingOrderList = new PriorityQueue<Long>();

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
            // TODO Auto-generated catch block
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

            WebClient restaurantClient =  WebClient.create("http://localhost:8081");
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
                    currentOrder.setStatus(1); //assigned

                    orderHistory.put(currentOrderId, currentOrder);

                    //currentOrderId++;

                    agentStatus.put(assignedAgent, UNAVAILABLE);


                } else {

                    Order currentOrder = new Order(custId, restId, itemId, qty);
                    currentOrder.setOrderId(currentOrderId);
                    currentOrder.setStatus(0); //Unassigned

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
                currentOrder.setStatus(1); //assigned

                //orderHistory.put(currentOrderId, currentOrder);

                agentStatus.put(agentId, UNAVAILABLE);
                
            } else {

                agentStatus.put(agentId, AVAIALBLE);
            }

            

        } 

        return true;
    }

    public Boolean agentSignOut(Long agentId) {

        if (agentStatus.get(agentId) == AVAIALBLE) {

            agentStatus.put(agentId, SIGNED_OUT);
        }

        return true;
    }

    public Boolean orderDelivered(Long orderId) {

        return true;
    }

    public ResponseEntity<String> getAgentStatus(long agentId) {
        int status = agentStatus.get(agentId);

        if (status == AVAIALBLE) {

            return new ResponseEntity<String>("{ \"agentId\":" + String.valueOf(agentId) + ", \"status\": \"available\"", HttpStatus.OK);

        } else if (status == UNAVAILABLE) {
            return new ResponseEntity<String>("{ \"agentId\":" + String.valueOf(agentId) + ", \"status\": \"unavailable\"", HttpStatus.OK);
        } 
        else {
            return new ResponseEntity<String>("{ \"agentId\":" + String.valueOf(agentId) + ", \"status\": \"signed-out\"", HttpStatus.OK);
        }
    }


}
