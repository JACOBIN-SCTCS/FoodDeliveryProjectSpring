package com.project.delivery.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.delivery.service.DBInitializer;
import com.project.delivery.service.DeliveryService;
import com.project.delivery.model.DeliveryAgent;
import com.project.delivery.model.Order;
import com.project.delivery.model.OrderStatus;


@RestController
class DeliveryController {

    @Autowired
    DeliveryService deliveryService;

    @Autowired
    DBInitializer dbInitializer;

    
    @PostMapping(path="/requestOrder",produces = "application/json",consumes="application/json")
    ResponseEntity<String> requestOrder(@RequestBody Order request)
    {
        return deliveryService.requestOrder(request.custId, request.restId, request.itemId, request.qty);
    }

    @PostMapping(path="/agentSignIn",consumes = "application/json")
    ResponseEntity<String> agentSignIn(@RequestBody DeliveryAgent agent)
    {
        deliveryService.agentSignIn(agent.getAgentId());
        
        return new ResponseEntity<String>("", HttpStatus.CREATED);
    }

    @PostMapping(path="/agentSignOut",consumes="application/json")
    ResponseEntity<String> agentSignOut(@RequestBody DeliveryAgent agent)
    {
        deliveryService.agentSignOut(agent.getAgentId());    

        return new ResponseEntity<String>("", HttpStatus.CREATED);
    }

    @PostMapping(path="/orderDelivered",consumes="application/json")
    ResponseEntity<String> orderDelivered(@RequestBody OrderStatus order)
    {
        deliveryService.orderDelivered(order.getOrderId());  
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/order/{num}")
    ResponseEntity<OrderStatus> getOrderStatus(@PathVariable("num") long num)
    {
        return deliveryService.getOrderStatus(num);
    }

    @GetMapping("/agent/{num}")
    ResponseEntity<DeliveryAgent>  getAgentStatus(@PathVariable("num") long num) {
        return deliveryService.getAgentStatus(num);    
        
    }

    @PostMapping("/reInitialize")
    ResponseEntity<String> reInitialize()
    {
        dbInitializer.reinitTables();

        /* To remove */ deliveryService.reInitialize();
        
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
