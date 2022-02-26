package com.project.delivery.repositories;

import com.project.delivery.entities.RestaurantEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository  extends JpaRepository<RestaurantEntity,Long>{
    
}
