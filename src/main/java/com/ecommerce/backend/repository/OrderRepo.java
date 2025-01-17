package com.ecommerce.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.entity.Order;

public interface OrderRepo extends JpaRepository<Order, Long> {
}
