package com.example.library;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.JsonNode;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:librarydoctestdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class OpenApiDocumentationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void operationIdsAreTheOnesTheRulesBindTo() throws Exception {
		JsonNode paths = GeneratedContract.generated(mockMvc).path("paths");
		Map<String, String> expected = Map.of(
				"/api/loans", "post:createLoan",
				"/api/loans/{id}", "get:getLoan",
				"/api/members/{memberId}/loans", "get:listMemberLoans",
				"/api/loans/{id}/status", "patch:updateLoanStatus");
		expected.forEach((path, op) -> {
			String[] parts = op.split(":");
			assertThat(paths.path(path).path(parts[0]).path("operationId").asString()).as(path).isEqualTo(parts[1]);
		});
		assertThat(paths.propertyNames()).hasSize(4);
	}

	@Test
	void loanSchemaDocumentsNullableTimestampsAndStatusEnum() throws Exception {
		JsonNode loan = GeneratedContract.generated(mockMvc).path("components").path("schemas").path("Loan");
		assertThat(loan.path("properties").path("status").path("enum").valueStream().map(JsonNode::asString).toList())
				.containsExactly("REQUESTED", "APPROVED", "REJECTED", "ISSUED", "RETURNED");
		for (String field : List.of("issuedAt", "returnedAt")) {
			assertThat(loan.path("properties").path(field).path("type").valueStream().map(JsonNode::asString).toList())
					.as(field).containsExactlyInAnyOrder("string", "null");
		}
	}

	@Test
	void generatedContractIsIdenticalToTheCommittedBaseline() throws Exception {
		assertThat(GeneratedContract.generated(mockMvc)).isEqualTo(GeneratedContract.baseline());
	}

}
