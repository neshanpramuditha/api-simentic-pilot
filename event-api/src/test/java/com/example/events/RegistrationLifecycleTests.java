package com.example.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
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

import com.example.events.model.Registration;
import com.example.events.model.RegistrationState;
import com.example.events.service.RegistrationService;
import com.example.events.store.RegistrationStore;
import com.jayway.jsonpath.JsonPath;

// Clean-mode behaviour (frozen before any validator rules were written).
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:eventstestdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class RegistrationLifecycleTests {

	private static final String TS = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?Z$";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RegistrationStore store;

	@Autowired
	private ApplicationContext context;

	@BeforeEach
	void reset() {
		store.deleteAll();
	}

	@Test
	void noDefectIsEnabledByDefault() {
		assertThat(context.getBeansOfType(RegistrationService.class).values()).singleElement()
				.matches(bean -> bean.getClass() == RegistrationService.class || bean.getClass().getSuperclass() == RegistrationService.class);
	}

	@Test
	void registerReturns201WithLocationAndRequestedState() throws Exception {
		mockMvc.perform(post("/v1/registrations").contentType(MediaType.APPLICATION_JSON)
				.content("{\"attendeeRef\":\"ATT-7\",\"eventCode\":\"JAVA-CONF-26\",\"seats\":2}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", matchesPattern("^/v1/registrations/REG-[A-Z0-9]{8}$")))
				.andExpect(jsonPath("$.registrationId", matchesPattern("^REG-[A-Z0-9]{8}$")))
				.andExpect(jsonPath("$.attendeeRef", is("ATT-7")))
				.andExpect(jsonPath("$.eventCode", is("JAVA-CONF-26")))
				.andExpect(jsonPath("$.seats", is(2)))
				.andExpect(jsonPath("$.state", is("REQUESTED")))
				.andExpect(jsonPath("$.requestedAt", matchesPattern(TS)))
				.andExpect(jsonPath("$.confirmedAt", nullValue()))
				.andExpect(jsonPath("$.checkedInAt", nullValue()))
				.andExpect(jsonPath("$.cancelledAt", nullValue()))
				.andExpect(jsonPath("$.cancellationReason", nullValue()));
		assertThat(store.count()).isEqualTo(1);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"{\"eventCode\":\"JAVA-CONF-26\",\"seats\":2}",
			"{\"attendeeRef\":\"7\",\"eventCode\":\"JAVA-CONF-26\",\"seats\":2}",
			"{\"attendeeRef\":\"ATT-7\",\"eventCode\":\"x\",\"seats\":2}",
			"{\"attendeeRef\":\"ATT-7\",\"eventCode\":\"JAVA-CONF-26\",\"seats\":0}",
			"{\"attendeeRef\":\"ATT-7\",\"eventCode\":\"JAVA-CONF-26\",\"seats\":5}",
			"{\"attendeeRef\":\"ATT-7\",\"eventCode\":\"JAVA-CONF-26\"}" })
	void invalidRegistrationIsRejected(String body) throws Exception {
		mockMvc.perform(post("/v1/registrations").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest());
		assertThat(store.count()).isZero();
	}

	@Test
	void fetchUnknownIs404() throws Exception {
		mockMvc.perform(get("/v1/registrations/REG-NOPE0000")).andExpect(status().isNotFound());
	}

	@Test
	void listUsesEnvelopeOrderedByRequestTime() throws Exception {
		String a = register("ATT-7");
		register("ATT-8");
		String b = register("ATT-7");
		mockMvc.perform(get("/v1/attendees/ATT-7/registrations"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.attendeeRef", is("ATT-7")))
				.andExpect(jsonPath("$.count", is(2)))
				.andExpect(jsonPath("$.items[*].registrationId", contains(a, b)));
		mockMvc.perform(get("/v1/attendees/ATT-99/registrations"))
				.andExpect(jsonPath("$.count", is(0))).andExpect(jsonPath("$.items", hasSize(0)));
	}

	@Test
	void confirmThenCheckInSetsTimestampsInOrder() throws Exception {
		String id = register("ATT-7");
		transition(id, "{\"to\":\"CONFIRMED\"}").andExpect(status().isOk())
				.andExpect(jsonPath("$.state", is("CONFIRMED"))).andExpect(jsonPath("$.confirmedAt", matchesPattern(TS)))
				.andExpect(jsonPath("$.checkedInAt", nullValue()));
		transition(id, "{\"to\":\"CHECKED_IN\"}").andExpect(status().isOk())
				.andExpect(jsonPath("$.state", is("CHECKED_IN"))).andExpect(jsonPath("$.checkedInAt", matchesPattern(TS)))
				.andExpect(jsonPath("$.confirmedAt", notNullValue()));
		Registration r = store.findById(id).orElseThrow();
		assertThat(r.getConfirmedAt()).isAfterOrEqualTo(r.getRequestedAt());
		assertThat(r.getCheckedInAt()).isAfterOrEqualTo(r.getConfirmedAt());
	}

	@Test
	void waitlistThenConfirmThenCancelKeepsConfirmedAtAndRecordsReason() throws Exception {
		String id = register("ATT-7");
		transition(id, "{\"to\":\"WAITLISTED\"}").andExpect(status().isOk()).andExpect(jsonPath("$.state", is("WAITLISTED")));
		transition(id, "{\"to\":\"CONFIRMED\"}").andExpect(status().isOk());
		transition(id, "{\"to\":\"CANCELLED\",\"reason\":\"Schedule conflict\"}").andExpect(status().isOk())
				.andExpect(jsonPath("$.state", is("CANCELLED"))).andExpect(jsonPath("$.cancelledAt", matchesPattern(TS)))
				.andExpect(jsonPath("$.cancellationReason", is("Schedule conflict")))
				.andExpect(jsonPath("$.confirmedAt", notNullValue()));
		assertThat(store.findById(id).orElseThrow().getState()).isEqualTo(RegistrationState.CANCELLED);
	}

	@Test
	void cancellationNeedsAReason() throws Exception {
		String id = register("ATT-7");
		transition(id, "{\"to\":\"CANCELLED\"}").andExpect(status().isBadRequest());
		transition(id, "{\"to\":\"CANCELLED\",\"reason\":\"  \"}").andExpect(status().isBadRequest());
		assertThat(store.findById(id).orElseThrow().getState()).isEqualTo(RegistrationState.REQUESTED);
	}

	static Stream<Arguments> forbidden() {
		List<Arguments> pairs = new ArrayList<>();
		for (RegistrationState from : RegistrationState.values()) {
			for (RegistrationState to : RegistrationState.values()) {
				if (!RegistrationService.isAllowed(from, to)) {
					pairs.add(Arguments.of(from, to));
				}
			}
		}
		return pairs.stream();
	}

	@ParameterizedTest
	@MethodSource("forbidden")
	void forbiddenTransitionIs422AndNothingIsStored(RegistrationState from, RegistrationState to) throws Exception {
		Registration r = new Registration("REG-TEST0001", "ATT-7", "JAVA-CONF-26", 1);
		r.setState(from);
		store.save(r);
		transition("REG-TEST0001", "{\"to\":\"" + to + "\",\"reason\":\"r\"}").andExpect(status().isUnprocessableContent());
		Registration after = store.findById("REG-TEST0001").orElseThrow();
		assertThat(after.getState()).isEqualTo(from);
		assertThat(after.getConfirmedAt()).isNull();
		assertThat(after.getCheckedInAt()).isNull();
		assertThat(after.getCancelledAt()).isNull();
		assertThat(after.getCancellationReason()).isNull();
	}

	@Test
	void forbiddenPairCountMatchesTheDocumentedTable() {
		assertThat(forbidden().count()).isEqualTo(25 - 7);
	}

	@Test
	void transitionOfUnknownRegistrationIs404() throws Exception {
		transition("REG-NOPE0000", "{\"to\":\"CONFIRMED\"}").andExpect(status().isNotFound());
	}

	@ParameterizedTest
	@ValueSource(strings = { "{\"to\":\"DONE\"}", "{}", "not json" })
	void invalidTransitionBodyIs400(String body) throws Exception {
		String id = register("ATT-7");
		transition(id, body).andExpect(status().isBadRequest());
		assertThat(store.findById(id).orElseThrow().getState()).isEqualTo(RegistrationState.REQUESTED);
	}

	private String register(String attendee) throws Exception {
		String json = mockMvc.perform(post("/v1/registrations").contentType(MediaType.APPLICATION_JSON)
				.content("{\"attendeeRef\":\"" + attendee + "\",\"eventCode\":\"JAVA-CONF-26\",\"seats\":1}"))
				.andExpect(status().isCreated()).andExpect(header().string("Location", endsWith("")))
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(json, "$.registrationId");
	}

	private ResultActions transition(String id, String body) throws Exception {
		return mockMvc.perform(post("/v1/registrations/{id}/transitions", id).contentType(MediaType.APPLICATION_JSON).content(body));
	}

}
