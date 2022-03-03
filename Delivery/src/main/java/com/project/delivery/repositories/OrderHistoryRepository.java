package com.project.delivery.repositories;

import java.util.List;

import javax.persistence.LockModeType;

import com.project.delivery.entities.OrderHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;


public interface OrderHistoryRepository extends JpaRepository<OrderHistory,Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OrderHistory> findByAssignedOrderByOrderIdAsc(int assigned);
}
