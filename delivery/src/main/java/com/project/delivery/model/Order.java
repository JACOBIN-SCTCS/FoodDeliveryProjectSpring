package com.project.delivery.model;

public class Order {

    public Long orderId, agentId, custId, restId, itemId, qty;
    int status;

    public Order(Long orderId) {
        this.orderId = orderId;
    }

    public Order(Long custId, Long restId, Long itemId, Long qty) {
        this.custId = custId;
        this.restId = restId;
        this.itemId = itemId;
        this.qty = qty;
    }


    public Order(Long orderId, int status) {
        this.orderId = orderId;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }


    
}
