package com.example.library.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.library.entity.Loan;
import com.example.library.enums.LoanStatus;
import com.example.library.repository.LoanRepository;
import com.example.library.service.LoanService;

// Clean-mode behaviour against a dedicated in-memory H2 database; the table is emptied before every test.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:librarytestdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class LoanControllerTests {

	private static final String RFC3339_UTC = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?Z$";

	private static final Map<LoanStatus, Set<LoanStatus>> ALLOWED = Map.of(
			LoanStatus.REQUESTED, EnumSet.of(LoanStatus.APPROVED, LoanStatus.REJECTED),
			LoanStatus.APPROVED, EnumSet.of(LoanStatus.ISSUED),
			LoanStatus.ISSUED, EnumSet.of(LoanStatus.RETURNED),
			LoanStatus.REJECTED, EnumSet.noneOf(LoanStatus.class),
			LoanStatus.RETURNED, EnumSet.noneOf(LoanStatus.class));

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private LoanRepository loanRepository;

	@Autowired
	private ApplicationContext context;

	@BeforeEach
	void resetDatabase() {
		loanRepository.deleteAll();
	}

	@Test
	void noDefectIsEnabledByDefault() {
		assertThat(context.getBeansOfType(LoanService.class).values())
				.singleElement()
				.extracting(Object::getClass)
				.matches(c -> c == LoanService.class || c.getSuperclass() == LoanService.class && c.getName().contains("SpringCGLIB"));
	}

	// --- create ---

	@Test
	void createValidLoanReturns201WithStatusRequested() throws Exception {
		mockMvc.perform(post("/api/loans")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"memberId": 42, "bookId": 1001}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", matchesPattern("^[0-9a-f-]{36}$")))
				.andExpect(jsonPath("$.memberId", is(42)))
				.andExpect(jsonPath("$.bookId", is(1001)))
				.andExpect(jsonPath("$.status", is("REQUESTED")))
				.andExpect(jsonPath("$.requestedAt", matchesPattern(RFC3339_UTC)))
				.andExpect(jsonPath("$.issuedAt", nullValue()))
				.andExpect(jsonPath("$.returnedAt", nullValue()));

		assertThat(loanRepository.count()).isEqualTo(1);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"{\"bookId\": 1001}",
			"{\"memberId\": 42}",
			"{\"memberId\": 0, \"bookId\": 1001}",
			"{\"memberId\": 42, \"bookId\": -1}",
			"{\"memberId\": \"x\", \"bookId\": 1001}",
			"not json" })
	void invalidCreateRequestIsRejectedAndNothingIsSaved(String body) throws Exception {
		mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());

		assertThat(loanRepository.count()).isZero();
	}

	// --- read ---

	@Test
	void getExistingLoanReturnsIt() throws Exception {
		Loan loan = loanRepository.save(new Loan(42L, 1001L));

		mockMvc.perform(get("/api/loans/{id}", loan.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(loan.getId().toString())))
				.andExpect(jsonPath("$.status", is("REQUESTED")));
	}

	@Test
	void getUnknownLoanReturns404() throws Exception {
		mockMvc.perform(get("/api/loans/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
	}

	@Test
	void getLoanWithMalformedIdReturns400() throws Exception {
		mockMvc.perform(get("/api/loans/not-a-uuid")).andExpect(status().isBadRequest());
	}

	@Test
	void memberLoansAreListedOldestFirstAndOnlyForThatMember() throws Exception {
		loanRepository.save(new Loan(42L, 1001L));
		loanRepository.save(new Loan(7L, 1002L));
		loanRepository.save(new Loan(42L, 1003L));

		mockMvc.perform(get("/api/members/{memberId}/loans", 42))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].bookId", contains(1001, 1003)));
		mockMvc.perform(get("/api/members/{memberId}/loans", 99))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	// --- status changes ---

	@Test
	void fullLifecycleSetsTimestampsOnIssueAndReturn() throws Exception {
		UUID id = loanRepository.save(new Loan(42L, 1001L)).getId();

		patchStatus(id, "APPROVED").andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("APPROVED")))
				.andExpect(jsonPath("$.issuedAt", nullValue()))
				.andExpect(jsonPath("$.returnedAt", nullValue()));
		patchStatus(id, "ISSUED").andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("ISSUED")))
				.andExpect(jsonPath("$.issuedAt", matchesPattern(RFC3339_UTC)))
				.andExpect(jsonPath("$.returnedAt", nullValue()));
		patchStatus(id, "RETURNED").andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("RETURNED")))
				.andExpect(jsonPath("$.issuedAt", notNullValue()))
				.andExpect(jsonPath("$.returnedAt", matchesPattern(RFC3339_UTC)));

		Loan persisted = loanRepository.findById(id).orElseThrow();
		assertThat(persisted.getStatus()).isEqualTo(LoanStatus.RETURNED);
		assertThat(persisted.getReturnedAt()).isAfterOrEqualTo(persisted.getIssuedAt());
	}

	@Test
	void rejectionIsAllowedFromRequested() throws Exception {
		UUID id = loanRepository.save(new Loan(42L, 1001L)).getId();
		patchStatus(id, "REJECTED").andExpect(status().isOk()).andExpect(jsonPath("$.status", is("REJECTED")));
	}

	static Stream<Arguments> forbiddenTransitions() {
		List<Arguments> pairs = new ArrayList<>();
		for (LoanStatus from : LoanStatus.values()) {
			for (LoanStatus to : LoanStatus.values()) {
				if (!ALLOWED.get(from).contains(to)) {
					pairs.add(Arguments.of(from, to));
				}
			}
		}
		return pairs.stream();
	}

	@ParameterizedTest
	@MethodSource("forbiddenTransitions")
	void forbiddenTransitionIsRejectedWith409AndNothingChanges(LoanStatus from, LoanStatus to) throws Exception {
		Loan loan = new Loan(42L, 1001L);
		loan.setStatus(from);
		Loan saved = loanRepository.save(loan);

		patchStatus(saved.getId(), to.name()).andExpect(status().isConflict());

		Loan persisted = loanRepository.findById(saved.getId()).orElseThrow();
		assertThat(persisted.getStatus()).isEqualTo(from);
		assertThat(persisted.getIssuedAt()).isNull();
		assertThat(persisted.getReturnedAt()).isNull();
	}

	@Test
	void statusChangeOfUnknownLoanReturns404() throws Exception {
		patchStatus(UUID.randomUUID(), "APPROVED").andExpect(status().isNotFound());
		assertThat(loanRepository.count()).isZero();
	}

	@ParameterizedTest
	@ValueSource(strings = { "{\"status\": \"LOST\"}", "{\"status\": null}", "{}" })
	void invalidStatusBodyIsRejectedWith400(String body) throws Exception {
		UUID id = loanRepository.save(new Loan(42L, 1001L)).getId();
		mockMvc.perform(patch("/api/loans/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());
		assertThat(loanRepository.findById(id).orElseThrow().getStatus()).isEqualTo(LoanStatus.REQUESTED);
	}

	private ResultActions patchStatus(UUID id, String status) throws Exception {
		return mockMvc.perform(patch("/api/loans/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"status": "%s"}
						""".formatted(status)));
	}

}
