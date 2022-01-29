package com.project.delivery.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.beans.factory.annotation.Autowired;

import com.project.delivery.service.DeliveryService;

import com.project.delivery.model.Order;
import com.project.delivery.model.Item;
import com.project.delivery.model.DeliveryAgent;


@RestController
class DeliveryController {

    @Autowired
    DeliveryService deliveryService;

    
    @PostMapping(path="/requestOrder",consumes="application/json")
    void requestOrder(@RequestBody Order request)
    {
        deliveryService.requestOrder(request.custId, request.restId, request.itemId, request.qty);
    }

    @PostMapping(path="/agentSignIn",consumes="application/json")
    void agentSignIn(@RequestParam Long agentId)
    {
        deliveryService.agentSignIn(agentId);    
    }

    @PostMapping(path="/agentSignOut",consumes="application/json")
    void agentSignOut(@RequestParam Long agentId)
    {
        deliveryService.agentSignOut(agentId);    
    }

    @PostMapping(path="/orderDelivered",consumes="application/json")
    void orderDelivered(@RequestParam Long orderId)
    {
        deliveryService.orderDelivered(orderId);  
    }

/*
    @GetMapping("/random")
    String printHello()
    {
        String str = new String();
        str += agentsList.get(0).agentId;
        str += agentsList.size();
        return str;
        
    }
*/
}
