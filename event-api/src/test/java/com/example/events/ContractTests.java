package com.example.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.JsonNode;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:eventscontractdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class ContractTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void operationIdsAreTheFrozenOnes() throws Exception {
		JsonNode paths = GeneratedContract.generated(mockMvc).path("paths");
		Map.of("/v1/registrations", "post:registerAttendee",
				"/v1/registrations/{registrationId}", "get:fetchRegistration",
				"/v1/registrations/{registrationId}/transitions", "post:transitionRegistration",
				"/v1/attendees/{attendeeRef}/registrations", "get:listAttendeeRegistrations").forEach((path, op) -> {
					String[] p = op.split(":");
					assertThat(paths.path(path).path(p[0]).path("operationId").asString()).as(path).isEqualTo(p[1]);
				});
		assertThat(paths.propertyNames()).hasSize(4);
	}

	@Test
	void generatedContractIsIdenticalToTheFrozenBaseline() throws Exception {
		assertThat(GeneratedContract.generated(mockMvc)).isEqualTo(GeneratedContract.baseline());
	}

}
