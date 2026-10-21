package com.example.library.service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.library.dto.CreateLoanRequest;
import com.example.library.dto.UpdateLoanStatusRequest;
import com.example.library.entity.Loan;
import com.example.library.enums.LoanStatus;
import com.example.library.repository.LoanRepository;

@Service
public class LoanService {

	// Allowed status transitions; any pair not listed here is rejected with 409 Conflict.
	private static final Map<LoanStatus, Set<LoanStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(LoanStatus.class);

	static {
		ALLOWED_TRANSITIONS.put(LoanStatus.REQUESTED, EnumSet.of(LoanStatus.APPROVED, LoanStatus.REJECTED));
		ALLOWED_TRANSITIONS.put(LoanStatus.APPROVED, EnumSet.of(LoanStatus.ISSUED));
		ALLOWED_TRANSITIONS.put(LoanStatus.ISSUED, EnumSet.of(LoanStatus.RETURNED));
		ALLOWED_TRANSITIONS.put(LoanStatus.REJECTED, EnumSet.noneOf(LoanStatus.class));
		ALLOWED_TRANSITIONS.put(LoanStatus.RETURNED, EnumSet.noneOf(LoanStatus.class));
	}

	private final LoanRepository loanRepository;

	public LoanService(LoanRepository loanRepository) {
		this.loanRepository = loanRepository;
	}

	@Transactional
	public Loan createLoan(CreateLoanRequest request) {
		return loanRepository.save(new Loan(request.memberId(), request.bookId()));
	}

	@Transactional(readOnly = true)
	public Loan getLoan(UUID id) {
		return loanRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan " + id + " not found"));
	}

	@Transactional(readOnly = true)
	public List<Loan> getLoansByMember(Long memberId) {
		return loanRepository.findByMemberIdOrderByRequestedAtAscIdAsc(memberId);
	}

	@Transactional
	public Loan updateStatus(UUID id, UpdateLoanStatusRequest request) {
		Loan loan = getLoan(id);
		LoanStatus current = loan.getStatus();
		LoanStatus target = request.status();

		if (!ALLOWED_TRANSITIONS.get(current).contains(target)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Invalid status transition: " + current + " -> " + target);
		}

		loan.setStatus(target);
		if (target == LoanStatus.ISSUED) {
			loan.setIssuedAt(Instant.now());
		}
		if (target == LoanStatus.RETURNED) {
			loan.setReturnedAt(Instant.now());
		}
		return loanRepository.save(loan);
	}

}
