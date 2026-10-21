package com.example.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for requesting a loan")
public record CreateLoanRequest(

		@Schema(description = "ID of the member who borrows the book", example = "42")
		@NotNull
		@Positive
		Long memberId,

		@Schema(description = "ID of the book to borrow", example = "1001")
		@NotNull
		@Positive
		Long bookId) {
}
