package com.project.delivery.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.delivery.service.DeliveryService;

import com.project.delivery.model.Order;
import com.project.delivery.model.Item;
import com.project.delivery.model.DeliveryAgent;


@RestController
class DeliveryController {

    @Autowired
    DeliveryService deliveryService;

    
    @PostMapping(path="/requestOrder",consumes="application/json")
    ResponseEntity<String> requestOrder(@RequestBody Order request)
    {
        return deliveryService.requestOrder(request.custId, request.restId, request.itemId, request.qty);
    }

    @PostMapping(path="/agentSignIn",consumes="application/json")
    ResponseEntity<String> agentSignIn(@RequestParam Long agentId)
    {
        deliveryService.agentSignIn(agentId);
        
        return new ResponseEntity<String>("", HttpStatus.CREATED);
    }

    @PostMapping(path="/agentSignOut",consumes="application/json")
    ResponseEntity<String> agentSignOut(@RequestParam Long agentId)
    {
        deliveryService.agentSignOut(agentId);    

        return new ResponseEntity<String>("", HttpStatus.CREATED);
    }

    @PostMapping(path="/orderDelivered",consumes="application/json")
    void orderDelivered(@RequestParam Long orderId)
    {
        deliveryService.orderDelivered(orderId);  
    }

    @GetMapping("/agent/{num}")
    ResponseEntity<String>  getAgentStatus(@PathVariable("num") long num) {
        return deliveryService.getAgentStatus(num);    
        
    }

}
