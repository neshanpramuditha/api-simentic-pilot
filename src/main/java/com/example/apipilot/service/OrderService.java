package com.example.apipilot.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.apipilot.dto.CreateOrderRequest;
import com.example.apipilot.dto.UpdateOrderStatusRequest;
import com.example.apipilot.entity.Order;
import com.example.apipilot.enums.OrderStatus;
import com.example.apipilot.repository.OrderRepository;

@Service
public class OrderService {

	// Allowed status transitions; any pair not listed here is rejected with 409 Conflict.
	private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

	static {
		ALLOWED_TRANSITIONS.put(OrderStatus.CREATED, EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED));
		ALLOWED_TRANSITIONS.put(OrderStatus.PAID, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
		ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED));
		ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.of(OrderStatus.COMPLETED));
		ALLOWED_TRANSITIONS.put(OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class));
		ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
	}

	private final OrderRepository orderRepository;

	public OrderService(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Transactional
	public Order createOrder(CreateOrderRequest request) {
		Order order = new Order(request.ownerUserId(), request.total());
		return orderRepository.save(order);
	}

	@Transactional(readOnly = true)
	public Order getOrder(Long id) {
		return orderRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + id + " not found"));
	}

	@Transactional(readOnly = true)
	public List<Order> getOrdersByUser(Long userId) {
		return orderRepository.findByOwnerUserId(userId);
	}

	@Transactional
	public Order updateStatus(Long id, UpdateOrderStatusRequest request) {
		Order order = getOrder(id);
		OrderStatus current = order.getStatus();
		OrderStatus target = request.status();

		if (!ALLOWED_TRANSITIONS.get(current).contains(target)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Invalid status transition: " + current + " -> " + target);
		}

		order.setStatus(target);
		if (target == OrderStatus.COMPLETED) {
			order.setCompletedAt(LocalDateTime.now());
		}
		return orderRepository.save(order);
	}

}
