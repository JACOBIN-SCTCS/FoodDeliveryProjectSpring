package com.project.delivery.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class AgentEntity {
    
    @Id
    @Column(name = "agentId")
    int agentId;
    @Column(name = "status")
    int status;

    AgentEntity()
    {

    }
    AgentEntity(int agentId, int status)
    {
        this.agentId = agentId;
        this.status = status;
    }
    public int getAgentId() {
        return this.agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


}
