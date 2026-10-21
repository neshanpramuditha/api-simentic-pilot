package com.example.library.defects;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.library.dto.UpdateLoanStatusRequest;
import com.example.library.entity.Loan;
import com.example.library.enums.LoanStatus;
import com.example.library.repository.LoanRepository;
import com.example.library.service.LoanService;

/**
 * INTENTIONAL DEFECT LIB-ST-01 (see docs/experiments/library-reuse/defects.md).
 * <p>
 * When enabled, {@code PATCH /api/loans/{id}/status} accepts the forbidden transition {@code REQUESTED -> ISSUED}
 * (issuing a book that was never approved) with 200, saves it and sets {@code issuedAt} as a normal issue would.
 * Every other request goes through the unchanged {@link LoanService}.
 * <p>
 * Disabled by default: this bean only exists when {@code library.defects.st01-requested-to-issued.enabled=true}.
 * Enable at most one LoanService-replacing defect at a time: only one {@code @Primary} may exist.
 */
@Service
@Primary
@ConditionalOnProperty(name = "library.defects.st01-requested-to-issued.enabled", havingValue = "true")
public class LibSt01RequestedToIssuedLoanService extends LoanService {

	private static final Logger log = LoggerFactory.getLogger(LibSt01RequestedToIssuedLoanService.class);

	private final LoanRepository loanRepository;

	public LibSt01RequestedToIssuedLoanService(LoanRepository loanRepository) {
		super(loanRepository);
		this.loanRepository = loanRepository;
		log.warn("DEFECT LIB-ST-01 ENABLED: PATCH /api/loans/{id}/status accepts REQUESTED -> ISSUED");
	}

	@Override
	@Transactional
	public Loan updateStatus(UUID id, UpdateLoanStatusRequest request) {
		Loan loan = getLoan(id);
		if (loan.getStatus() == LoanStatus.REQUESTED && request.status() == LoanStatus.ISSUED) {
			loan.setStatus(LoanStatus.ISSUED);
			loan.setIssuedAt(Instant.now());
			return loanRepository.save(loan);
		}
		return super.updateStatus(id, request);
	}

}
