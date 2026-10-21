package com.example.library.defects;

import static com.example.library.defects.DefectTestSupport.patchStatus;
import static com.example.library.defects.DefectTestSupport.saveLoan;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

// Verifies the LIB-RI-01 switch itself; clean-mode behaviour is covered by LoanControllerTests, which never enables it.
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:libri01testdb;DB_CLOSE_DELAY=-1",
		"library.defects.ri01-returned-without-timestamp.enabled=true" })
@AutoConfigureMockMvc
class LibRi01ReturnedWithoutTimestampTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LoanRepository loanRepository;

	@BeforeEach
	void resetDatabase() {
		loanRepository.deleteAll();
	}

	@Test
	void returnIsAcceptedButReturnedAtStaysNullInResponsesAndDatabase() throws Exception {
		Loan loan = saveLoan(loanRepository, LoanStatus.APPROVED);
		patchStatus(mockMvc, loan.getId(), LoanStatus.ISSUED).andExpect(status().isOk());
		patchStatus(mockMvc, loan.getId(), LoanStatus.RETURNED)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("RETURNED")))
				.andExpect(jsonPath("$.issuedAt", notNullValue()))
				.andExpect(jsonPath("$.returnedAt", nullValue()));
		mockMvc.perform(get("/api/loans/{id}", loan.getId()))
				.andExpect(jsonPath("$.status", is("RETURNED")))
				.andExpect(jsonPath("$.returnedAt", nullValue()));

		Loan persisted = loanRepository.findById(loan.getId()).orElseThrow();
		assertThat(persisted.getStatus()).isEqualTo(LoanStatus.RETURNED);
		assertThat(persisted.getReturnedAt()).isNull();
	}

	@Test
	void otherTransitionsBehaveAsInCleanMode() throws Exception {
		Loan loan = saveLoan(loanRepository, LoanStatus.APPROVED);
		patchStatus(mockMvc, loan.getId(), LoanStatus.ISSUED)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.issuedAt", notNullValue()))
				.andExpect(jsonPath("$.returnedAt", nullValue()));
		Loan requested = saveLoan(loanRepository, LoanStatus.REQUESTED);
		patchStatus(mockMvc, requested.getId(), LoanStatus.RETURNED).andExpect(status().isConflict());
		assertThat(loanRepository.findById(requested.getId()).orElseThrow().getStatus()).isEqualTo(LoanStatus.REQUESTED);
	}

	@Test
	void generatedContractIsIdenticalToTheCleanBaseline() throws Exception {
		assertThat(GeneratedContract.generated(mockMvc)).isEqualTo(GeneratedContract.baseline());
	}

}
