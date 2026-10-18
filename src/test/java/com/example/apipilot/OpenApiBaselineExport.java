package com.example.apipilot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Writes the generated OpenAPI document to docs/openapi-baseline.json.
 * <p>
 * Not part of the normal test run: the class name does not match Surefire's default includes,
 * and it only runs when {@code -Dopenapi.export=true} is set. Regenerate with:
 *
 * <pre>
 * mvnw test -Dtest=OpenApiBaselineExport -Dopenapi.export=true
 * </pre>
 */
@EnabledIfSystemProperty(named = "openapi.export", matches = "true")
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:openapiexportdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class OpenApiBaselineExport {

	private static final Path BASELINE = Path.of("docs", "openapi-baseline.json");

	// Sorted keys, LF line endings and exact decimals keep the file byte-for-byte stable between exports.
	private static final JsonMapper MAPPER = JsonMapper.builder()
			.enable(SerializationFeature.INDENT_OUTPUT)
			.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
			.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
			.build();

	@Autowired
	private MockMvc mockMvc;

	@Test
	void exportBaseline() throws Exception {
		// Port 8080 so the generated "servers" entry matches the running application.
		String json = mockMvc.perform(get("/v3/api-docs").with(request -> {
					request.setServerPort(8080);
					return request;
				}))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

		Map<?, ?> document = MAPPER.readValue(json, Map.class);
		String pretty = MAPPER.writeValueAsString(document).replace("\r\n", "\n") + "\n";
		Files.createDirectories(BASELINE.getParent());
		Files.writeString(BASELINE, pretty, StandardCharsets.UTF_8);

		JsonNode saved = MAPPER.readTree(Files.readString(BASELINE, StandardCharsets.UTF_8));
		assertThat(saved.path("openapi").asString()).startsWith("3.");
		assertThat(saved.path("paths").propertyNames())
				.containsExactlyInAnyOrder("/api/orders", "/api/orders/{id}", "/api/orders/{id}/status",
						"/api/users/{userId}/orders");
		assertThat(saved.path("paths").path("/api/orders").has("post")).isTrue();
		assertThat(saved.path("paths").path("/api/orders/{id}").has("get")).isTrue();
		assertThat(saved.path("paths").path("/api/orders/{id}/status").has("patch")).isTrue();
		assertThat(saved.path("paths").path("/api/users/{userId}/orders").has("get")).isTrue();
	}

}
