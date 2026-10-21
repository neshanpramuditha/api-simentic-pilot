package com.example.library.defects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.library.entity.Loan;
import com.example.library.enums.LoanStatus;
import com.example.library.repository.LoanRepository;

final class DefectTestSupport {

	private DefectTestSupport() {
	}

	static Loan saveLoan(LoanRepository repository, LoanStatus status) {
		Loan loan = new Loan(42L, 1001L);
		loan.setStatus(status);
		return repository.save(loan);
	}

	static ResultActions patchStatus(MockMvc mockMvc, UUID id, LoanStatus status) throws Exception {
		return mockMvc.perform(patch("/api/loans/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"status": "%s"}
						""".formatted(status.name())));
	}

}
