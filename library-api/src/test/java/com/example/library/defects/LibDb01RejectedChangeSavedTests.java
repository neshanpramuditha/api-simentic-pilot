package com.example.library.defects;

import static com.example.library.defects.DefectTestSupport.patchStatus;
import static com.example.library.defects.DefectTestSupport.saveLoan;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.example.library.GeneratedContract;
import com.example.library.entity.Loan;
import com.example.library.enums.LoanStatus;
import com.example.library.repository.LoanRepository;

// Verifies the LIB-DB-01 switch itself; clean-mode behaviour is covered by LoanControllerTests, which never enables it.
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:libdb01testdb;DB_CLOSE_DELAY=-1",
		"library.defects.db01-rejected-change-saved.enabled=true" })
@AutoConfigureMockMvc
class LibDb01RejectedChangeSavedTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LoanRepository loanRepository;

	@BeforeEach
	void resetDatabase() {
		loanRepository.deleteAll();
	}

	@Test
	void rejectedTransitionReturns409ButIsPersisted() throws Exception {
		Loan returned = saveLoan(loanRepository, LoanStatus.RETURNED);
		patchStatus(mockMvc, returned.getId(), LoanStatus.ISSUED).andExpect(status().isConflict());
		Loan afterReturned = loanRepository.findById(returned.getId()).orElseThrow();
		assertThat(afterReturned.getStatus()).isEqualTo(LoanStatus.ISSUED);
		assertThat(afterReturned.getIssuedAt()).isNotNull();

		Loan rejected = saveLoan(loanRepository, LoanStatus.REJECTED);
		patchStatus(mockMvc, rejected.getId(), LoanStatus.APPROVED).andExpect(status().isConflict());
		assertThat(loanRepository.findById(rejected.getId()).orElseThrow().getStatus()).isEqualTo(LoanStatus.APPROVED);
	}

	@Test
	void allowedTransitionsAndUnknownLoansBehaveAsInCleanMode() throws Exception {
		Loan loan = saveLoan(loanRepository, LoanStatus.REQUESTED);
		patchStatus(mockMvc, loan.getId(), LoanStatus.APPROVED)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("APPROVED")));
		assertThat(loanRepository.findById(loan.getId()).orElseThrow().getStatus()).isEqualTo(LoanStatus.APPROVED);

		patchStatus(mockMvc, UUID.randomUUID(), LoanStatus.APPROVED).andExpect(status().isNotFound());
		assertThat(loanRepository.count()).isEqualTo(1);
	}

	@Test
	void generatedContractIsIdenticalToTheCleanBaseline() throws Exception {
		assertThat(GeneratedContract.generated(mockMvc)).isEqualTo(GeneratedContract.baseline());
	}

}
