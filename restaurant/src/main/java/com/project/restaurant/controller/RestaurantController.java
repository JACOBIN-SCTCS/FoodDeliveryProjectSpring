package com.project.restaurant.controller;

import com.project.restaurant.model.RestaurantInventory;
import com.project.restaurant.service.RestaurantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RestaurantController {
    
    @Autowired
    RestaurantService restaurantService;


    // Accepts Order invoked by the Delivery service if the required quantity of delivery item is present.
    @PostMapping(path="/acceptOrder",consumes="application/json")
    int acceptOrder(@RequestBody RestaurantInventory request)
    {
        return restaurantService.acceptOrder(request.restId, request.itemId, request.qty);    
    }


    // Refills the item x at the inventory by quantity y.
    @PostMapping(path="/refillItem",consumes="application/json")
    int refillItem(@RequestBody RestaurantInventory request)
    {
        return restaurantService.fillItem(request.restId, request.itemId, request.qty);     
    }

    

    // Reinitializes the inventory
    @PostMapping(path="/reInitialize",consumes="application/json")
    int reInitialize(@RequestBody RestaurantInventory request)
    {
        return restaurantService.reInitialize();    
    }


}