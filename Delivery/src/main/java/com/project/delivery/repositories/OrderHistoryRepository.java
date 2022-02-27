package com.project.delivery.repositories;

import com.project.delivery.entities.OrderHistory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory,Long>{
    
}
