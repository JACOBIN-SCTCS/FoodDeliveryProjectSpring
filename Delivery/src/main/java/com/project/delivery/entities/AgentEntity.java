package com.project.delivery.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class AgentEntity {
    
    @Id
    @Column(name = "agentId")
    Long agentId;
    @Column(name = "status")
    int status;

    AgentEntity()
    {

    }
    AgentEntity(Long agentId, int status)
    {
        this.agentId = agentId;
        this.status = status;
    }
    public Long getAgentId() {
        return this.agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


}
