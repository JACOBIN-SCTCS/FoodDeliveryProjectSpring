package com.project.delivery.repositories;

import com.project.delivery.entities.CurrentState;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrentStateRepository extends JpaRepository<CurrentState,Integer>{
    
}
