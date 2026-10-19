package com.example.apipilot.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

// Clean mode: drives one order through the full lifecycle over HTTP, checking response and persisted state at every step.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:semantictestdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class TransitionSequenceTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderRepository orderRepository;

	@BeforeEach
	void resetDatabase() {
		orderRepository.deleteAll();
	}

	@Test
	void fullLifecycleThenCancellingACompletedOrderIsRejected() throws Exception {
		String created = mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"ownerUserId": 7, "total": 49.99}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status", is("CREATED")))
				.andReturn().getResponse().getContentAsString();
		long id = ((Number) JsonPath.read(created, "$.id")).longValue();
		assertThat(persisted(id).getStatus()).isEqualTo(OrderStatus.CREATED);

		for (OrderStatus next : new OrderStatus[] { OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED }) {
			patchStatus(id, next)
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status", is(next.name())))
					.andExpect(jsonPath("$.completedAt", nullValue()));
			assertThat(persisted(id).getStatus()).isEqualTo(next);
		}

		patchStatus(id, OrderStatus.COMPLETED)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("COMPLETED")))
				.andExpect(jsonPath("$.completedAt", notNullValue()));
		Order completed = persisted(id);
		assertThat(completed.getStatus()).isEqualTo(OrderStatus.COMPLETED);
		assertThat(completed.getCompletedAt()).isNotNull();

		patchStatus(id, OrderStatus.CANCELLED)
				.andExpect(status().isConflict());
		Order afterRejection = persisted(id);
		assertThat(afterRejection.getStatus()).isEqualTo(OrderStatus.COMPLETED);
		assertThat(afterRejection.getCompletedAt()).isEqualTo(completed.getCompletedAt());
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
