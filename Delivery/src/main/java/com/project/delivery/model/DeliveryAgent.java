package com.project.delivery.model;

public class DeliveryAgent
{
    Long agentId;
    String status;
    public DeliveryAgent()
    {
        ;
    }
    public DeliveryAgent(Long agentId)
    {
        this.agentId = agentId;
        this.status = new String("signed-out");
    }   
    public String getStatus()
    {
        return this.status;
    } 
    public long getAgentId()
    {
        return this.agentId;
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
