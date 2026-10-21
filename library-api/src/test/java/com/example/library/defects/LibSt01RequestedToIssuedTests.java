package com.example.library.defects;

import static com.example.library.defects.DefectTestSupport.patchStatus;
import static com.example.library.defects.DefectTestSupport.saveLoan;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

// Verifies the LIB-ST-01 switch itself; clean-mode behaviour is covered by LoanControllerTests, which never enables it.
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:libst01testdb;DB_CLOSE_DELAY=-1",
		"library.defects.st01-requested-to-issued.enabled=true" })
@AutoConfigureMockMvc
class LibSt01RequestedToIssuedTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LoanRepository loanRepository;

	@BeforeEach
	void resetDatabase() {
		loanRepository.deleteAll();
	}

	@Test
	void requestedToIssuedIsAcceptedAndPersisted() throws Exception {
		Loan loan = saveLoan(loanRepository, LoanStatus.REQUESTED);
		patchStatus(mockMvc, loan.getId(), LoanStatus.ISSUED)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("ISSUED")))
				.andExpect(jsonPath("$.issuedAt", notNullValue()));
		assertThat(loanRepository.findById(loan.getId()).orElseThrow().getStatus()).isEqualTo(LoanStatus.ISSUED);
	}

	@Test
	void otherForbiddenTransitionsAreStillRejected() throws Exception {
		Loan rejected = saveLoan(loanRepository, LoanStatus.REJECTED);
		patchStatus(mockMvc, rejected.getId(), LoanStatus.ISSUED).andExpect(status().isConflict());
		Loan requested = saveLoan(loanRepository, LoanStatus.REQUESTED);
		patchStatus(mockMvc, requested.getId(), LoanStatus.RETURNED).andExpect(status().isConflict());
		assertThat(loanRepository.findById(requested.getId()).orElseThrow().getStatus()).isEqualTo(LoanStatus.REQUESTED);
	}

	@Test
	void allowedTransitionsBehaveAsInCleanMode() throws Exception {
		Loan loan = saveLoan(loanRepository, LoanStatus.REQUESTED);
		patchStatus(mockMvc, loan.getId(), LoanStatus.APPROVED).andExpect(status().isOk()).andExpect(jsonPath("$.status", is("APPROVED")));
		patchStatus(mockMvc, loan.getId(), LoanStatus.ISSUED).andExpect(status().isOk()).andExpect(jsonPath("$.status", is("ISSUED")));
	}

	@Test
	void generatedContractIsIdenticalToTheCleanBaseline() throws Exception {
		assertThat(GeneratedContract.generated(mockMvc)).isEqualTo(GeneratedContract.baseline());
	}

}
