package com.project.delivery.model;

public class Order {

    public Long orderId, agentId, custId, restId, itemId, qty;
    int status;

  
    public Order(Long custId, Long restId, Long itemId, Long qty) {
        this.custId = custId;
        this.restId = restId;
        this.itemId = itemId;
        this.qty = qty;
        this.agentId = -1l;
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

    public void setCustId(Long custId)
    {
        this.custId = custId;
    }

    public Long getCustId()
    {
        return this.custId;
    }

    public void setRestId(Long restId)
    {
        this.restId = restId;
    }
    public Long getRestId()
    {
        return this.restId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }
    public Long getItemId()
    {
        return this.itemId;
    }


    public void setQty(Long qty)
    {
        this.qty = qty;
    }
    public Long getQty()
    {
        return this.qty;
    }


}
