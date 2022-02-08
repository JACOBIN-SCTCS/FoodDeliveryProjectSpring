package com.project.delivery.model;

public class OrderStatus 
{
    Long orderId;
    String status;
    Long agentId;

    public OrderStatus()
    {
        ;
    }

    public  OrderStatus(Long orderId)
    {
        this.orderId = orderId;
    }
    public Long getOrderId()
    {
        return this.orderId;
    }
    public String getStatus()
    {
        return this.status;
    }
    public Long getAgentId()
    {
        return this.agentId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }
    public void setAgentId(Long agentId)
    {
        this.agentId = agentId;
    }

}
