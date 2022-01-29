package com.project.delivery.service;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

import com.project.delivery.model.Item;
import com.project.delivery.model.WalletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class DeliveryService {

    List<Item> itemList;
    HashMap<Long,Integer> agentStatus;
    PriorityQueue<Long> availableAgents;
    List<Long> pendingOrderList;

    final int SIGNED_OUT = 0;
    final int AVAIALBLE = 1;
    final int UNAVAILABLE = 2;
  

    public void initialData() throws Exception {

        itemList = new ArrayList<Item>();

        String userDirectory = new File("").getAbsolutePath();
        System.out.println(userDirectory);
        File file = new File(userDirectory+ "/target/initialData.txt");
        Scanner sc = new Scanner(file);
        
        agentStatus = new HashMap<Long,Integer>();
        availableAgents = new PriorityQueue<Long>();
        pendingOrderList = new ArrayList<Long>();

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
    
    public Boolean requestOrder(Long custId, Long restId, Long itemId, Long qty) {

        Long totalPrice;

        for (Item item: itemList) {

            if (item.getRestId().equals(restId) && item.getItemId().equals(itemId)) {
                
                totalPrice = item.getPrice() * qty;
                break;

            } 
        }

      WebClient client =  WebClient.create("http://localhost:8082");
      WalletRequest payload = new WalletRequest(custId, totalPrice)  ;  
      Mono<ResponseEntity<String>> retvalue = client.post()
      .uri("/addBalance")
      .contentType(MediaType.APPLICATION_JSON)
      .body(Mono.just(payload), WalletRequest.class)
      .retrieve()
      .toEntity(String.class);
  

		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_CREATED) { // success
			
            System.out.print("Success");
		} else {
			System.out.println("GET request not worked");
		}


        return true;
    }

    public Boolean agentSignIn(Long agentId) {

        return true;
    }

    public Boolean agentSignOut(Long agentId) {

        return true;
    }

    public Boolean orderDelivered(Long orderId) {

        return true;
    }


}
