package com.example.apipilot.defects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import org.springframework.test.web.servlet.ResultActions;

import com.example.apipilot.entity.Order;
import com.example.apipilot.enums.OrderStatus;
import com.example.apipilot.repository.OrderRepository;
import com.jayway.jsonpath.JsonPath;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Verifies the ST-01 switch itself; clean-mode behaviour is covered by the other test classes, which never enable it.
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:st01testdb;DB_CLOSE_DELAY=-1",
		"pilot.defects.st01-completed-to-cancelled.enabled=true" })
@AutoConfigureMockMvc
class St01CompletedToCancelledTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderRepository orderRepository;

	@BeforeEach
	void resetDatabase() {
		orderRepository.deleteAll();
	}

	@Test
	void fullLifecycleThenCancellingACompletedOrderIsAccepted() throws Exception {
		String created = mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"ownerUserId": 7, "total": 49.99}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status", is("CREATED")))
				.andReturn().getResponse().getContentAsString();
		long id = ((Number) JsonPath.read(created, "$.id")).longValue();

		for (OrderStatus next : new OrderStatus[] { OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED,
				OrderStatus.COMPLETED }) {
			patchStatus(id, next)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status", is(next.name())));
			assertThat(persisted(id).getStatus()).isEqualTo(next);
		}
		Order completed = persisted(id);

		patchStatus(id, OrderStatus.CANCELLED)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("CANCELLED")))
				.andExpect(jsonPath("$.completedAt", notNullValue()));

		Order cancelled = persisted(id);
		assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
		// Side effect documented in docs/experiments/st-01/design.md: completedAt is not cleared.
		assertThat(cancelled.getCompletedAt()).isEqualTo(completed.getCompletedAt());
	}

	@Test
	void otherForbiddenTransitionsAreStillRejected() throws Exception {
		Order completed = saveOrder(OrderStatus.COMPLETED);
		patchStatus(completed.getId(), OrderStatus.PAID).andExpect(status().isConflict());
		assertThat(persisted(completed.getId()).getStatus()).isEqualTo(OrderStatus.COMPLETED);

		Order created = saveOrder(OrderStatus.CREATED);
		patchStatus(created.getId(), OrderStatus.DELIVERED).andExpect(status().isConflict());
		assertThat(persisted(created.getId()).getStatus()).isEqualTo(OrderStatus.CREATED);

		Order cancelled = saveOrder(OrderStatus.CANCELLED);
		patchStatus(cancelled.getId(), OrderStatus.CANCELLED).andExpect(status().isConflict());
		assertThat(persisted(cancelled.getId()).getStatus()).isEqualTo(OrderStatus.CANCELLED);
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

	private Order saveOrder(OrderStatus status) {
		Order order = new Order(7L, new BigDecimal("10.00"));
		order.setStatus(status);
		return orderRepository.save(order);
	}

	private Order persisted(long id) {
		return orderRepository.findById(id).orElseThrow();
	}

	private ResultActions patchStatus(long id, OrderStatus status) throws Exception {
		return mockMvc.perform(patch("/api/orders/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"status": "%s"}
						""".formatted(status.name())));
	}

}
