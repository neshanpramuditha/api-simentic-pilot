package com.example.apipilot.defects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.apipilot.repository.OrderRepository;
import com.jayway.jsonpath.JsonPath;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Verifies the SV-01 switch itself; clean-mode behaviour is covered by the other test classes, which never enable it.
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:sv01testdb;DB_CLOSE_DELAY=-1",
		"pilot.defects.sv01-total-as-string.enabled=true" })
@AutoConfigureMockMvc
class Sv01TotalAsStringTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderRepository orderRepository;

	@BeforeEach
	void resetDatabase() {
		orderRepository.deleteAll();
	}

	@Test
	void postReturnsTotalAsStringAndEverythingElseUnchanged() throws Exception {
		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"ownerUserId": 7, "total": 49.99}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.total", is("49.99")))
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.ownerUserId", is(7)))
				.andExpect(jsonPath("$.status", is("CREATED")))
				.andExpect(jsonPath("$.completedAt", nullValue()));

		assertThat(orderRepository.findAll()).singleElement()
				.satisfies(order -> assertThat(order.getTotal()).isEqualByComparingTo("49.99"));
	}

	@Test
	void getIsNotAffected() throws Exception {
		String created = mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"ownerUserId": 7, "total": 49.99}
						"""))
				.andReturn().getResponse().getContentAsString();
		Integer id = JsonPath.read(created, "$.id");

		mockMvc.perform(get("/api/orders/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total", is(49.99)));
	}

	@Test
	void generatedContractIsIdenticalToTheCleanBaseline() throws Exception {
		JsonMapper mapper = JsonMapper.builder().build();
		String generated = mockMvc.perform(get("/v3/api-docs").with(request -> {
					request.setServerPort(8080);
					return request;
				}))
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		JsonNode baseline = mapper.readTree(Files.readString(Path.of("docs", "openapi-baseline.json")));

		assertThat(mapper.readTree(generated)).isEqualTo(baseline);
	}

}
