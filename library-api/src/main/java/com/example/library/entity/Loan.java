package com.example.library.entity;

import java.time.Instant;
import java.util.UUID;

import com.example.library.enums.LoanStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Timestamps are Instants, serialized as RFC 3339 UTC strings ("...Z"), so they satisfy "format: date-time".
@Schema(description = "A book loan")
@Entity
@Table(name = "loans")
public class Loan {

	@Schema(description = "Loan ID, assigned by the server", example = "3f2c8a51-7d0e-4c8e-9a43-2b1f6c9d0e15",
			accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Schema(description = "ID of the member who borrows the book", example = "42",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@Column(nullable = false)
	private Long memberId;

	@Schema(description = "ID of the book being borrowed", example = "1001",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@Column(nullable = false)
	private Long bookId;

	@Schema(description = "Current lifecycle status", example = "REQUESTED",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LoanStatus status;

	@Schema(description = "When the loan was requested", example = "2026-10-19T09:15:02.123456Z",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@Column(nullable = false)
	private Instant requestedAt;

	@Schema(description = "When the book was issued; set on the move to ISSUED, null before that",
			example = "2026-10-19T09:20:41.5Z", implementation = String.class, format = "date-time",
			types = { "string", "null" }, requiredMode = Schema.RequiredMode.REQUIRED)
	private Instant issuedAt;

	@Schema(description = "When the book was returned; set on the move to RETURNED, null before that",
			example = "2026-11-02T16:03:10.9Z", implementation = String.class, format = "date-time",
			types = { "string", "null" }, requiredMode = Schema.RequiredMode.REQUIRED)
	private Instant returnedAt;

	protected Loan() {
		// required by JPA
	}

	public Loan(Long memberId, Long bookId) {
		this.memberId = memberId;
		this.bookId = bookId;
		this.status = LoanStatus.REQUESTED;
		this.requestedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public Long getMemberId() {
		return memberId;
	}

	public Long getBookId() {
		return bookId;
	}

	public LoanStatus getStatus() {
		return status;
	}

	public void setStatus(LoanStatus status) {
		this.status = status;
	}

	public Instant getRequestedAt() {
		return requestedAt;
	}

	public Instant getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(Instant issuedAt) {
		this.issuedAt = issuedAt;
	}

	public Instant getReturnedAt() {
		return returnedAt;
	}

	public void setReturnedAt(Instant returnedAt) {
		this.returnedAt = returnedAt;
	}

}
