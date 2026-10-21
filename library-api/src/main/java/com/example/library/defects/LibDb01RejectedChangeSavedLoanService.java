package com.example.library.defects;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.library.dto.UpdateLoanStatusRequest;
import com.example.library.entity.Loan;
import com.example.library.enums.LoanStatus;
import com.example.library.repository.LoanRepository;
import com.example.library.service.LoanService;

/**
 * INTENTIONAL DEFECT LIB-DB-01 (see docs/experiments/library-reuse/defects.md).
 * <p>
 * When enabled, a status change that the unchanged {@link LoanService} rejects with 409 is still written to the
 * database (with the timestamp the target status would normally set): the response is the same 409 as in clean
 * mode, but the loan now has the forbidden status. The accept/reject decision itself is the clean service's.
 * <p>
 * Disabled by default: this bean only exists when {@code library.defects.db01-rejected-change-saved.enabled=true}.
 * Enable at most one LoanService-replacing defect at a time: only one {@code @Primary} may exist.
 */
@Service
@Primary
@ConditionalOnProperty(name = "library.defects.db01-rejected-change-saved.enabled", havingValue = "true")
public class LibDb01RejectedChangeSavedLoanService extends LoanService {

	private static final Logger log = LoggerFactory.getLogger(LibDb01RejectedChangeSavedLoanService.class);

	private final LoanRepository loanRepository;

	public LibDb01RejectedChangeSavedLoanService(LoanRepository loanRepository) {
		super(loanRepository);
		this.loanRepository = loanRepository;
		log.warn("DEFECT LIB-DB-01 ENABLED: PATCH /api/loans/{id}/status saves rejected (409) status changes");
	}

	@Override
	@Transactional(noRollbackFor = ResponseStatusException.class)
	public Loan updateStatus(UUID id, UpdateLoanStatusRequest request) {
		try {
			return super.updateStatus(id, request);
		} catch (ResponseStatusException rejected) {
			if (rejected.getStatusCode() == HttpStatus.CONFLICT) {
				Loan loan = getLoan(id);
				loan.setStatus(request.status());
				if (request.status() == LoanStatus.ISSUED) {
					loan.setIssuedAt(Instant.now());
				}
				if (request.status() == LoanStatus.RETURNED) {
					loan.setReturnedAt(Instant.now());
				}
				loanRepository.save(loan);
			}
			throw rejected;
		}
	}

}
