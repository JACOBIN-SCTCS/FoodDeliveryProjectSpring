package com.project.delivery.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OrderHistory 
{
    @Id
    @Column(name = "orderId")
    int orderId;

    @Column(name = "restId")
    int restId;

    @Column(name = "custId")
    int custId;
    
    @Column(name = "itemId")
    int itemId;

    @Column(name="qty")
    int qty;
    
    @Column(name = "agentId")
    int agentId;

    @Column(name = "assigned")
    int assigned;

    public OrderHistory()
    {

    }
    public OrderHistory(int orderId, int restId, int custId, int itemId, int qty, int agentId, int assigned)
    {
        this.orderId = orderId;
        this.restId = restId;
        this.custId = custId;
        this.itemId = itemId;
        this.qty = qty;
        this.agentId = agentId;
        this.assigned = assigned;
    }
    public int getOrderId() {
        return this.orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getRestId() {
        return this.restId;
    }

    public void setRestId(int restId) {
        this.restId = restId;
    }

    public int getCustId() {
        return this.custId;
    }

    public void setCustId(int custId) {
        this.custId = custId;
    }

    public int getItemId() {
        return this.itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getQty() {
        return this.qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getAgentId() {
        return this.agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public int getAssigned() {
        return this.assigned;
    }

    public void setAssigned(int assigned) {
        this.assigned = assigned;
    }

}
