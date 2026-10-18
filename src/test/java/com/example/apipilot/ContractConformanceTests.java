package com.example.apipilot;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.apipilot.entity.Order;
import com.example.apipilot.enums.OrderStatus;
import com.example.apipilot.repository.OrderRepository;
import com.jayway.jsonpath.JsonPath;

// Checks that actual responses match what the generated OpenAPI document says about them.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:semantictestdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class ContractConformanceTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderRepository orderRepository;

	@BeforeEach
	void resetDatabase() {
		orderRepository.deleteAll();
	}

	@Test
	void completedAtIsNullBeforeCompletion() throws Exception {
		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"ownerUserId": 7, "total": 49.99}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.completedAt", nullValue()));
	}

	@Test
	void serializedCompletedAtMatchesDocumentedPattern() throws Exception {
		String spec = mockMvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();
		String pattern = JsonPath.read(spec, "$.components.schemas.Order.properties.completedAt.pattern");

		Order order = new Order(7L, new BigDecimal("10.00"));
		order.setStatus(OrderStatus.DELIVERED);
		order = orderRepository.save(order);

		mockMvc.perform(patch("/api/orders/{id}/status", order.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"status": "COMPLETED"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completedAt", matchesPattern(pattern)));

		mockMvc.perform(get("/api/orders/{id}", order.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completedAt", matchesPattern(pattern)));
	}

	@Test
	void nonIntegerPathParametersReturnDocumented400() throws Exception {
		mockMvc.perform(get("/api/orders/{id}", "abc")).andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/users/{userId}/orders", "abc")).andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/orders/{id}", "99999999999999999999")).andExpect(status().isBadRequest());

		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(jsonPath("$.paths['/api/orders/{id}'].get.responses", hasKey("400")))
				.andExpect(jsonPath("$.paths['/api/users/{userId}/orders'].get.responses", hasKey("400")));
	}

}
