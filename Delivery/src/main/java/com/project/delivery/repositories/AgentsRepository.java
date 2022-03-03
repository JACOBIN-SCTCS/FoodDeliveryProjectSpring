package com.project.delivery.repositories;

import java.util.List;

import javax.persistence.LockModeType;

import com.project.delivery.entities.AgentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface AgentsRepository extends JpaRepository<AgentEntity,Long>{
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<AgentEntity> findByStatusOrderByAgentIdAsc(int status);
}
