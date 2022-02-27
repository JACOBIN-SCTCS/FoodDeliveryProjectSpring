package com.project.delivery.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OrderHistory 
{
    @Id
    @Column(name = "orderId")
    Long orderId;

    @Column(name = "restId")
    Long restId;

    @Column(name = "custId")
    Long custId;
    
    @Column(name = "itemId")
    Long itemId;

    @Column(name="qty")
    Long qty;
    
    @Column(name = "agentId")
    Long agentId;

    @Column(name = "assigned")
    int assigned;

    public OrderHistory()
    {

    }
    public OrderHistory(Long orderId, Long restId, Long custId, Long itemId, Long qty, Long agentId, int assigned)
    {
        this.orderId = orderId;
        this.restId = restId;
        this.custId = custId;
        this.itemId = itemId;
        this.qty = qty;
        this.agentId = agentId;
        this.assigned = assigned;
    }
    public Long getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getRestId() {
        return this.restId;
    }

    public void setRestId(Long restId) {
        this.restId = restId;
    }

    public Long getCustId() {
        return this.custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getQty() {
        return this.qty;
    }

    public void setQty(Long qty) {
        this.qty = qty;
    }

    public Long getAgentId() {
        return this.agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public int getAssigned() {
        return this.assigned;
    }

    public void setAssigned(int assigned) {
        this.assigned = assigned;
    }

}
