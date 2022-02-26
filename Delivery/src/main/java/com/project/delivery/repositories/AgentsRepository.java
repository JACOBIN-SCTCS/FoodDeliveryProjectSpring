package com.project.delivery.repositories;

import com.project.delivery.entities.AgentEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentsRepository extends JpaRepository<AgentEntity,Long>{
    
}
