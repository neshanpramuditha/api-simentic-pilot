package com.example.library.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.dto.CreateLoanRequest;
import com.example.library.dto.UpdateLoanStatusRequest;
import com.example.library.entity.Loan;
import com.example.library.service.LoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Loans", description = "Request book loans, look them up, and move them through the lending lifecycle")
@RestController
@RequestMapping("/api")
public class LoanController {

	private final LoanService loanService;

	public LoanController(LoanService loanService) {
		this.loanService = loanService;
	}

	@Operation(operationId = "createLoan", summary = "Request a loan",
			description = "Creates a loan request with status REQUESTED; issuedAt and returnedAt are null.")
	@ApiResponse(responseCode = "201", description = "Loan requested")
	@ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content)
	@PostMapping("/loans")
	@ResponseStatus(HttpStatus.CREATED)
	public Loan createLoan(@Valid @RequestBody CreateLoanRequest request) {
		return loanService.createLoan(request);
	}

	@Operation(operationId = "getLoan", summary = "Get a loan by ID")
	@ApiResponse(responseCode = "200", description = "Loan found")
	@ApiResponse(responseCode = "400", description = "ID is not a UUID", content = @Content)
	@ApiResponse(responseCode = "404", description = "No loan with this ID", content = @Content)
	@GetMapping("/loans/{id}")
	public Loan getLoan(@Parameter(description = "Loan ID", example = "3f2c8a51-7d0e-4c8e-9a43-2b1f6c9d0e15")
			@PathVariable UUID id) {
		return loanService.getLoan(id);
	}

	@Operation(operationId = "listMemberLoans", summary = "List a member's loans",
			description = "Returns every loan of the member, oldest request first; an empty list if the member has none.")
	@ApiResponse(responseCode = "200", description = "Loans of the member")
	@ApiResponse(responseCode = "400", description = "Member ID is not a valid 64-bit integer", content = @Content)
	@GetMapping("/members/{memberId}/loans")
	public List<Loan> listMemberLoans(@Parameter(description = "Member ID", example = "42") @PathVariable Long memberId) {
		return loanService.getLoansByMember(memberId);
	}

	@Operation(operationId = "updateLoanStatus", summary = "Change a loan's status",
			description = "Allowed transitions: REQUESTED -> APPROVED | REJECTED; APPROVED -> ISSUED; ISSUED -> RETURNED. "
					+ "REJECTED and RETURNED are final. Moving to ISSUED sets issuedAt; moving to RETURNED sets returnedAt.")
	@ApiResponse(responseCode = "200", description = "Status updated")
	@ApiResponse(responseCode = "400", description = "ID is not a UUID, or request body failed validation", content = @Content)
	@ApiResponse(responseCode = "404", description = "No loan with this ID", content = @Content)
	@ApiResponse(responseCode = "409", description = "Transition not allowed from the current status", content = @Content)
	@PatchMapping("/loans/{id}/status")
	public Loan updateLoanStatus(@Parameter(description = "Loan ID", example = "3f2c8a51-7d0e-4c8e-9a43-2b1f6c9d0e15")
			@PathVariable UUID id, @Valid @RequestBody UpdateLoanStatusRequest request) {
		return loanService.updateStatus(id, request);
	}

}
