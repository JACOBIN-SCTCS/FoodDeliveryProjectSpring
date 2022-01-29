package com.project.restaurant.service;

import java.util.ArrayList;
import java.util.*;
import java.io.File;
import java.util.Scanner;

import com.project.restaurant.model.RestaurantInventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RestaurantService {

    List<RestaurantInventory> restaurantInventory;

    public void initialData() throws Exception {


        restaurantInventory = new ArrayList<RestaurantInventory>();

        String userDirectory = new File("").getAbsolutePath();
        System.out.println(userDirectory);
        File file = new File(userDirectory+ "/target/initialData.txt");
        Scanner sc = new Scanner(file);
 
        while (sc.hasNextLine()) {

            String str = sc.nextLine();
            System.out.println(str);
            String[] splited = str.split("\\s+");
            if (splited[0].indexOf('*') > -1) break;
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
                
                RestaurantInventory res = new RestaurantInventory(restId, itemId, qty);
                restaurantInventory.add(res);
                
                
            }

        }
            
    }

    public RestaurantService() {


        try {
            initialData();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    public Boolean acceptOrder(Long restId, Long itemId, Long qty) {

        for (RestaurantInventory item: restaurantInventory) {

            if (item.getRestId().equals(restId) && item.getItemId().equals(itemId)) {
                
                if (item.getQty() >= qty) {

                    item.setQty(item.getQty() - qty);
                    return true;

                } else {
                    return false;
                }
            } 
        }
        return false;
    }

    public Boolean fillItem(Long restId, Long itemId, Long qty) {

        for (RestaurantInventory item: restaurantInventory) {

            if (item.getRestId().equals(restId) && item.getItemId().equals(itemId)) {
                
                item.setQty(item.getQty() + qty);
                return true;
            } 
        }
        return false;
    }

    public Boolean reInitialize() {

        try {
            initialData();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;

    }
    
}
