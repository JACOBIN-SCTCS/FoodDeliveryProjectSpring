package com.project.restaurant.controller;

import com.project.restaurant.model.RestaurantInventory;
import com.project.restaurant.service.RestaurantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
class RestaurantController {
    
    @Autowired
    RestaurantService restaurantService;

    
    // Accepts Order invoked by the Delivery service if the required quantity of delivery item is present.
    @PostMapping(path="/acceptOrder",consumes="application/json")
    ResponseEntity<String> acceptOrder(@RequestBody RestaurantInventory request)
    {
        if (restaurantService.acceptOrder(request.restId, request.itemId, request.qty)) 
            return new ResponseEntity<String>("Created", HttpStatus.CREATED);
        else  
            return new ResponseEntity<String>("Gone", HttpStatus.GONE);
    }


    // Refills the item x at the inventory by quantity y.
    @PostMapping(path="/refillItem",consumes="application/json")
    ResponseEntity<String> refillItem(@RequestBody RestaurantInventory request)
    {
        if (restaurantService.fillItem(request.restId, request.itemId, request.qty))
            return new ResponseEntity<String>("Created", HttpStatus.CREATED);
        else  
            return new ResponseEntity<String>("Item not found", HttpStatus.NOT_FOUND);
    }

    

    // Reinitializes the inventory
    @PostMapping(path="/reInitialize",consumes="application/json")
    ResponseEntity<String> reInitialize(@RequestBody RestaurantInventory request)
    {
        if (restaurantService.reInitialize()) 
            return new ResponseEntity<String>("Created", HttpStatus.CREATED);
        else
            return new ResponseEntity<String>("Exception Occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }


}