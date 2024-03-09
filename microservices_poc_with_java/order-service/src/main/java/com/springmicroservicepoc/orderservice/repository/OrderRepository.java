package com.springmicroservicepoc.orderservice.repository;


import com.springmicroservicepoc.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
