package com.example.apipilot.dto;

import com.example.apipilot.enums.OrderStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for changing an order's status")
public record UpdateOrderStatusRequest(

		@Schema(description = "Target status; must be an allowed transition from the current status", example = "PAID")
		@NotNull
		OrderStatus status) {
}
