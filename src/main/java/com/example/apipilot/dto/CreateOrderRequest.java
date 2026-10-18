package com.example.apipilot.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for creating an order")
public record CreateOrderRequest(

		@Schema(description = "ID of the user who owns the order", example = "7")
		@NotNull
		@Positive
		Long ownerUserId,

		// @Digits matches the column's precision/scale so values are not silently rounded on save.
		@Schema(description = "Order total; greater than zero, at most 2 decimal places", example = "49.99")
		@NotNull
		@DecimalMin(value = "0.00", inclusive = false)
		@Digits(integer = 17, fraction = 2)
		BigDecimal total) {
}
