package com.example.apipilot.defects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Verifies the DB-02 switch itself; clean-mode behaviour is covered by the other test classes, which never enable it.
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:db02testdb;DB_CLOSE_DELAY=-1",
		"pilot.defects.db02-rejected-change-saved.enabled=true" })
@AutoConfigureMockMvc
class Db02RejectedChangeSavedTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderRepository orderRepository;

	@BeforeEach
	void resetDatabase() {
		orderRepository.deleteAll();
	}

	@Test
	void rejectedTransitionReturns409ButIsPersisted() throws Exception {
		Order completed = saveOrder(OrderStatus.COMPLETED);
		patchStatus(completed.getId(), OrderStatus.CANCELLED).andExpect(status().isConflict());
		assertThat(persisted(completed.getId()).getStatus()).isEqualTo(OrderStatus.CANCELLED);

		Order created = saveOrder(OrderStatus.CREATED);
		patchStatus(created.getId(), OrderStatus.DELIVERED).andExpect(status().isConflict());
		assertThat(persisted(created.getId()).getStatus()).isEqualTo(OrderStatus.DELIVERED);
	}

	@Test
	void allowedTransitionsAndUnknownOrdersBehaveAsInCleanMode() throws Exception {
		Order created = saveOrder(OrderStatus.CREATED);
		patchStatus(created.getId(), OrderStatus.PAID)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("PAID")));
		assertThat(persisted(created.getId()).getStatus()).isEqualTo(OrderStatus.PAID);

		patchStatus(999_999L, OrderStatus.PAID).andExpect(status().isNotFound());
		assertThat(orderRepository.count()).isEqualTo(1);
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
