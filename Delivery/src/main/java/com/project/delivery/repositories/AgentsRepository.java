package com.project.delivery.repositories;

import java.util.List;

import com.project.delivery.entities.AgentEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentsRepository extends JpaRepository<AgentEntity,Long>{
    List<AgentEntity> findByStatusOrderByAgentIdAsc(int status);
}
