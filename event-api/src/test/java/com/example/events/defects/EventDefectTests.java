package com.example.events.defects;

import static com.example.events.defects.DefectSupport.fetch;
import static com.example.events.defects.DefectSupport.stored;
import static com.example.events.defects.DefectSupport.transition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.example.events.GeneratedContract;
import com.example.events.model.Registration;
import com.example.events.model.RegistrationState;
import com.example.events.store.RegistrationStore;

/**
 * One nested class per defect switch, each in its own application context with only that switch on. Each proves the
 * defect's effect, that neighbouring behaviour is still clean, and that the published contract is unchanged.
 * Clean behaviour is covered by RegistrationLifecycleTests, which never enables a switch.
 */
class EventDefectTests {

	abstract static class Base {

		@Autowired
		MockMvc mockMvc;

		@Autowired
		RegistrationStore store;

		@BeforeEach
		void reset() {
			store.deleteAll();
		}

		Registration persisted(String id) {
			return store.findById(id).orElseThrow();
		}

		@Test
		void contractIsIdenticalToTheFrozenBaseline() throws Exception {
			assertThat(GeneratedContract.generated(mockMvc)).isEqualTo(GeneratedContract.baseline());
		}

	}

	@Nested
	@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:evtst01;DB_CLOSE_DELAY=-1",
			"events.defects.st01-waitlist-confirm-rejected.enabled=true" })
	@AutoConfigureMockMvc
	class St01 extends Base {

		@Test
		void waitlistToConfirmedIsRejectedAndNothingStored() throws Exception {
			String id = stored(store, RegistrationState.WAITLISTED).getRegistrationId();
			transition(mockMvc, id, "CONFIRMED", null).andExpect(status().isUnprocessableContent());
			assertThat(persisted(id).getState()).isEqualTo(RegistrationState.WAITLISTED);
		}

		@Test
		void otherTransitionsAreClean() throws Exception {
			String id = stored(store, RegistrationState.REQUESTED).getRegistrationId();
			transition(mockMvc, id, "CONFIRMED", null).andExpect(status().isOk()).andExpect(jsonPath("$.state", is("CONFIRMED")));
			String w = stored(store, RegistrationState.WAITLISTED).getRegistrationId();
			transition(mockMvc, w, "CANCELLED", "x").andExpect(status().isOk());
			transition(mockMvc, w, "CONFIRMED", null).andExpect(status().isUnprocessableContent());
		}

	}

	@Nested
	@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:evtdb01;DB_CLOSE_DELAY=-1",
			"events.defects.db01-rejected-change-saved.enabled=true" })
	@AutoConfigureMockMvc
	class Db01 extends Base {

		@Test
		void rejectedTransitionIs422ButStored() throws Exception {
			String id = stored(store, RegistrationState.WAITLISTED).getRegistrationId();
			transition(mockMvc, id, "CHECKED_IN", null).andExpect(status().isUnprocessableContent());
			assertThat(persisted(id).getState()).isEqualTo(RegistrationState.CHECKED_IN);
			assertThat(persisted(id).getCheckedInAt()).isNotNull();
		}

		@Test
		void allowedTransitionsValidationAnd404AreClean() throws Exception {
			String id = stored(store, RegistrationState.REQUESTED).getRegistrationId();
			transition(mockMvc, id, "WAITLISTED", null).andExpect(status().isOk());
			transition(mockMvc, id, "CANCELLED", null).andExpect(status().isBadRequest());
			assertThat(persisted(id).getState()).isEqualTo(RegistrationState.WAITLISTED);
			transition(mockMvc, "REG-NOPE0000", "CONFIRMED", null).andExpect(status().isNotFound());
		}

	}

	@Nested
	@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:evtdb02;DB_CLOSE_DELAY=-1",
			"events.defects.db02-cancellation-not-saved.enabled=true" })
	@AutoConfigureMockMvc
	class Db02 extends Base {

		@Test
		void cancellationIsReturnedButNotStored() throws Exception {
			String id = stored(store, RegistrationState.CONFIRMED).getRegistrationId();
			transition(mockMvc, id, "CANCELLED", "Schedule conflict").andExpect(status().isOk())
					.andExpect(jsonPath("$.state", is("CANCELLED"))).andExpect(jsonPath("$.cancelledAt", notNullValue()))
					.andExpect(jsonPath("$.cancellationReason", is("Schedule conflict")));
			assertThat(persisted(id).getState()).isEqualTo(RegistrationState.CONFIRMED);
			assertThat(persisted(id).getCancellationReason()).isNull();
			fetch(mockMvc, id).andExpect(jsonPath("$.state", is("CONFIRMED")));
		}

		@Test
		void otherTransitionsRejectionsAndValidationAreClean() throws Exception {
			String id = stored(store, RegistrationState.CONFIRMED).getRegistrationId();
			transition(mockMvc, id, "CANCELLED", null).andExpect(status().isBadRequest());
			transition(mockMvc, id, "CHECKED_IN", null).andExpect(status().isOk());
			assertThat(persisted(id).getState()).isEqualTo(RegistrationState.CHECKED_IN);
			transition(mockMvc, id, "CANCELLED", "late").andExpect(status().isUnprocessableContent());
			assertThat(persisted(id).getState()).isEqualTo(RegistrationState.CHECKED_IN);
		}

	}

	@Nested
	@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:evtri01;DB_CLOSE_DELAY=-1",
			"events.defects.ri01-checkin-without-timestamp.enabled=true" })
	@AutoConfigureMockMvc
	class Ri01 extends Base {

		@Test
		void checkInIsStoredWithoutCheckedInAt() throws Exception {
			String id = stored(store, RegistrationState.REQUESTED).getRegistrationId();
			transition(mockMvc, id, "CONFIRMED", null).andExpect(jsonPath("$.confirmedAt", notNullValue()));
			transition(mockMvc, id, "CHECKED_IN", null).andExpect(status().isOk())
					.andExpect(jsonPath("$.state", is("CHECKED_IN"))).andExpect(jsonPath("$.checkedInAt", nullValue()));
			assertThat(persisted(id).getState()).isEqualTo(RegistrationState.CHECKED_IN);
			assertThat(persisted(id).getCheckedInAt()).isNull();
		}

	}

	@Nested
	@SpringBootTest(properties = { "spring.datasource.url=jdbc:h2:mem:evtcf01;DB_CLOSE_DELAY=-1",
			"events.defects.cf01-checkin-before-confirmation.enabled=true" })
	@AutoConfigureMockMvc
	class Cf01 extends Base {

		@Test
		void checkedInAtPrecedesConfirmedAt() throws Exception {
			String id = stored(store, RegistrationState.REQUESTED).getRegistrationId();
			Thread.sleep(5);
			transition(mockMvc, id, "CONFIRMED", null).andExpect(status().isOk());
			transition(mockMvc, id, "CHECKED_IN", null).andExpect(status().isOk()).andExpect(jsonPath("$.checkedInAt", notNullValue()));
			Registration r = persisted(id);
			assertThat(r.getCheckedInAt()).isEqualTo(r.getRequestedAt()).isBefore(r.getConfirmedAt());
		}

		@Test
		void otherTimestampsAreClean() throws Exception {
			String id = stored(store, RegistrationState.REQUESTED).getRegistrationId();
			transition(mockMvc, id, "CANCELLED", "x").andExpect(jsonPath("$.cancelledAt", notNullValue()))
					.andExpect(jsonPath("$.checkedInAt", nullValue()));
		}

	}

}
