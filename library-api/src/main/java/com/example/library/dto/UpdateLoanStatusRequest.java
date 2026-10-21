package com.example.library.dto;

import com.example.library.enums.LoanStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for changing a loan's status")
public record UpdateLoanStatusRequest(

		@Schema(description = "Target status; must be an allowed transition from the current status", example = "APPROVED")
		@NotNull
		LoanStatus status) {
}
