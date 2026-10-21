package com.example.library.defects;

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
 * INTENTIONAL DEFECT LIB-RI-01 (see docs/experiments/library-reuse/defects.md).
 * <p>
 * When enabled, the allowed transition {@code ISSUED -> RETURNED} is accepted and saved as in clean mode, but the
 * loan is saved without {@code returnedAt}. Every later response for the loan therefore shows a RETURNED loan with
 * a null return time. The transition decision and every other request are the unchanged {@link LoanService}'s.
 * <p>
 * Disabled by default: this bean only exists when {@code library.defects.ri01-returned-without-timestamp.enabled=true}.
 * Enable at most one LoanService-replacing defect at a time: only one {@code @Primary} may exist.
 */
@Service
@Primary
@ConditionalOnProperty(name = "library.defects.ri01-returned-without-timestamp.enabled", havingValue = "true")
public class LibRi01ReturnedWithoutTimestampLoanService extends LoanService {

	private static final Logger log = LoggerFactory.getLogger(LibRi01ReturnedWithoutTimestampLoanService.class);

	private final LoanRepository loanRepository;

	public LibRi01ReturnedWithoutTimestampLoanService(LoanRepository loanRepository) {
		super(loanRepository);
		this.loanRepository = loanRepository;
		log.warn("DEFECT LIB-RI-01 ENABLED: PATCH /api/loans/{id}/status saves RETURNED loans without returnedAt");
	}

	@Override
	@Transactional
	public Loan updateStatus(UUID id, UpdateLoanStatusRequest request) {
		Loan loan = super.updateStatus(id, request);
		if (request.status() == LoanStatus.RETURNED) {
			loan.setReturnedAt(null);
			return loanRepository.save(loan);
		}
		return loan;
	}

}
