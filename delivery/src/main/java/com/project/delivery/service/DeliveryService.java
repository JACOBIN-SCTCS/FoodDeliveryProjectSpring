package com.project.delivery.service;

import java.util.ArrayList;
import java.util.*;
import java.io.File;
import java.util.Scanner;

import com.project.delivery.model.Order;
import com.project.delivery.model.Item;
import com.project.delivery.model.DeliveryAgent;

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

        for (RestaurantInventory item: restaurantInventory) {

            if (item.getRestId().equals(restId) && item.getItemId().equals(itemId)) {
                
                totalPrice = item.getPrice() * qty;
                break;

            } 
        }

        String POST_PARAMS = "{custId:"+ Long.toString(custId) + ", amount:" + Long.toString(totalPrice) +"}";
        //To Wallet
        URL obj = new URL("htttp://127.0.0.1:8082");
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		//con.setRequestProperty("User-Agent", USER_AGENT);

        con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();


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
