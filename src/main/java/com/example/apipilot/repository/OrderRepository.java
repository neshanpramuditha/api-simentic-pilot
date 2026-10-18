package com.example.apipilot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.apipilot.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByOwnerUserId(Long ownerUserId);

}
