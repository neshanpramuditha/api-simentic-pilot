package com.example.apipilot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.apipilot.dto.CreateOrderRequest;
import com.example.apipilot.dto.UpdateOrderStatusRequest;
import com.example.apipilot.entity.Order;
import com.example.apipilot.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Orders", description = "Create orders, look them up, and move them through the status lifecycle")
@RestController
@RequestMapping("/api")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@Operation(summary = "Create an order", description = "Creates a new order with status CREATED.")
	@ApiResponse(responseCode = "201", description = "Order created")
	@ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content)
	@PostMapping("/orders")
	@ResponseStatus(HttpStatus.CREATED)
	public Order createOrder(@Valid @RequestBody CreateOrderRequest request) {
		return orderService.createOrder(request);
	}

	@Operation(summary = "Get an order by ID")
	@ApiResponse(responseCode = "200", description = "Order found")
	@ApiResponse(responseCode = "404", description = "No order with this ID", content = @Content)
	@GetMapping("/orders/{id}")
	public Order getOrder(@Parameter(description = "Order ID", example = "1") @PathVariable Long id) {
		return orderService.getOrder(id);
	}

	@Operation(summary = "List a user's orders",
			description = "Returns every order owned by the user; an empty list if the user has none.")
	@ApiResponse(responseCode = "200", description = "Orders owned by the user")
	@GetMapping("/users/{userId}/orders")
	public List<Order> getOrdersByUser(@Parameter(description = "Owner user ID", example = "7") @PathVariable Long userId) {
		return orderService.getOrdersByUser(userId);
	}

	@Operation(summary = "Change an order's status",
			description = "Allowed transitions: CREATED -> PAID | CANCELLED; PAID -> SHIPPED | CANCELLED; "
					+ "SHIPPED -> DELIVERED; DELIVERED -> COMPLETED. COMPLETED and CANCELLED are final. "
					+ "Moving to COMPLETED sets completedAt.")
	@ApiResponse(responseCode = "200", description = "Status updated")
	@ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content)
	@ApiResponse(responseCode = "404", description = "No order with this ID", content = @Content)
	@ApiResponse(responseCode = "409", description = "Transition not allowed from the current status", content = @Content)
	@PatchMapping("/orders/{id}/status")
	public Order updateStatus(@Parameter(description = "Order ID", example = "1") @PathVariable Long id,
			@Valid @RequestBody UpdateOrderStatusRequest request) {
		return orderService.updateStatus(id, request);
	}

}
