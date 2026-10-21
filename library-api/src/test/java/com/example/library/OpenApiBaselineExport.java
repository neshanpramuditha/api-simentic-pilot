package com.example.library;

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
 * Writes the generated OpenAPI document to library-api/openapi-baseline.json (same approach as the Order API's
 * export). Not part of the normal test run; regenerate from the clean API with:
 *
 * <pre>
 * mvnw -f library-api/pom.xml test -Dtest=OpenApiBaselineExport -Dopenapi.export=true
 * </pre>
 */
@EnabledIfSystemProperty(named = "openapi.export", matches = "true")
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:libraryexportdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class OpenApiBaselineExport {

	static final Path BASELINE = Path.of("openapi-baseline.json");

	private static final JsonMapper MAPPER = JsonMapper.builder()
			.enable(SerializationFeature.INDENT_OUTPUT)
			.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
			.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
			.build();

	@Autowired
	private MockMvc mockMvc;

	@Test
	void exportBaseline() throws Exception {
		// Port 8081 so the generated "servers" entry matches the running application.
		String json = mockMvc.perform(get("/v3/api-docs").with(request -> {
					request.setServerPort(8081);
					return request;
				}))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

		Map<?, ?> document = MAPPER.readValue(json, Map.class);
		String pretty = MAPPER.writeValueAsString(document).replace("\r\n", "\n") + "\n";
		Files.writeString(BASELINE, pretty, StandardCharsets.UTF_8);

		JsonNode saved = MAPPER.readTree(Files.readString(BASELINE, StandardCharsets.UTF_8));
		assertThat(saved.path("openapi").asString()).startsWith("3.");
		assertThat(saved.path("paths").propertyNames())
				.containsExactlyInAnyOrder("/api/loans", "/api/loans/{id}", "/api/loans/{id}/status",
						"/api/members/{memberId}/loans");
	}

}
