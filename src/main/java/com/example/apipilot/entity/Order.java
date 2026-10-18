package com.example.apipilot.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.apipilot.enums.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// "ORDER" is a reserved SQL keyword, so the table is named "orders".
@Schema(description = "An order")
@Entity
@Table(name = "orders")
public class Order {

	@Schema(description = "Order ID, assigned by the server", example = "1",
			accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Schema(description = "ID of the user who owns the order", example = "7",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@Column(nullable = false)
	private Long ownerUserId;

	@Schema(description = "Current lifecycle status", example = "CREATED",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus status;

	@Schema(description = "Order total", example = "49.99",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal total;

	@Schema(description = "When the order reached COMPLETED; null until then", example = "2026-10-18T20:11:35")
	private LocalDateTime completedAt;

	protected Order() {
		// required by JPA
	}

	public Order(Long ownerUserId, BigDecimal total) {
		this.ownerUserId = ownerUserId;
		this.total = total;
		this.status = OrderStatus.CREATED;
	}

	public Long getId() {
		return id;
	}

	public Long getOwnerUserId() {
		return ownerUserId;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

}
