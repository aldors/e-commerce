package com.aldo.e_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aldo.e_commerce.entity.CarritoItem;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long>{
    
}
