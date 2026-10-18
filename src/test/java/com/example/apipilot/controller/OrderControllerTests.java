package com.example.apipilot.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.apipilot.entity.Order;
import com.example.apipilot.enums.OrderStatus;
import com.example.apipilot.repository.OrderRepository;

// Runs against a dedicated in-memory H2 database; the table is emptied before every test.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:semantictestdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class OrderControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderRepository orderRepository;

	@BeforeEach
	void resetDatabase() {
		orderRepository.deleteAll();
	}

	// --- create ---

	@Test
	void createValidOrderReturns201WithStatusCreated() throws Exception {
		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"ownerUserId": 7, "total": 49.99}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", notNullValue()))
				.andExpect(jsonPath("$.ownerUserId", is(7)))
				.andExpect(jsonPath("$.total", is(49.99)))
				.andExpect(jsonPath("$.status", is("CREATED")))
				.andExpect(jsonPath("$.completedAt", nullValue()));

		assertThat(orderRepository.count()).isEqualTo(1);
	}

	@Test
	void createOrderWithMissingOwnerUserIdIsRejected() throws Exception {
		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"total": 49.99}
						"""))
				.andExpect(status().isBadRequest());

		assertThat(orderRepository.count()).isZero();
	}

	@ParameterizedTest
	@ValueSource(strings = { "0", "0.00", "-5.00" })
	void createOrderWithZeroOrNegativeTotalIsRejected(String total) throws Exception {
		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"ownerUserId": 7, "total": %s}
						""".formatted(total)))
				.andExpect(status().isBadRequest());

		assertThat(orderRepository.count()).isZero();
	}

	// --- read ---

	@Test
	void getExistingOrderReturns200() throws Exception {
		Order order = saveOrder(7L, "25.50", OrderStatus.CREATED);

		mockMvc.perform(get("/api/orders/{id}", order.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(order.getId().intValue())))
				.andExpect(jsonPath("$.ownerUserId", is(7)))
				.andExpect(jsonPath("$.total", is(25.50)))
				.andExpect(jsonPath("$.status", is("CREATED")));
	}

	@Test
	void getNonexistentOrderReturns404() throws Exception {
		mockMvc.perform(get("/api/orders/{id}", 999_999L))
				.andExpect(status().isNotFound());
	}

	@Test
	void getOrdersByUserReturnsOnlyThatUsersOrders() throws Exception {
		Order first = saveOrder(7L, "10.00", OrderStatus.CREATED);
		Order second = saveOrder(7L, "20.00", OrderStatus.PAID);
		saveOrder(8L, "30.00", OrderStatus.CREATED);

		mockMvc.perform(get("/api/users/{userId}/orders", 7L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].ownerUserId", everyItem(is(7))))
				.andExpect(jsonPath("$[*].id", containsInAnyOrder(
						first.getId().intValue(), second.getId().intValue())));
	}

	// --- status transitions ---

	@Test
	void createdToPaidSucceeds() throws Exception {
		Order order = saveOrder(7L, "10.00", OrderStatus.CREATED);

		patchStatus(order.getId(), "PAID")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("PAID")))
				.andExpect(jsonPath("$.completedAt", nullValue()));

		assertThat(reload(order).getStatus()).isEqualTo(OrderStatus.PAID);
	}

	@Test
	void createdToCancelledSucceeds() throws Exception {
		Order order = saveOrder(7L, "10.00", OrderStatus.CREATED);

		patchStatus(order.getId(), "CANCELLED")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("CANCELLED")));

		assertThat(reload(order).getStatus()).isEqualTo(OrderStatus.CANCELLED);
	}

	@Test
	void createdToDeliveredIsRejectedWith409() throws Exception {
		Order order = saveOrder(7L, "10.00", OrderStatus.CREATED);

		patchStatus(order.getId(), "DELIVERED")
				.andExpect(status().isConflict());

		assertThat(reload(order).getStatus()).isEqualTo(OrderStatus.CREATED);
	}

	@Test
	void deliveredToCompletedSucceedsAndSetsCompletedAt() throws Exception {
		Order order = saveOrder(7L, "10.00", OrderStatus.DELIVERED);

		patchStatus(order.getId(), "COMPLETED")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("COMPLETED")))
				.andExpect(jsonPath("$.completedAt", notNullValue()));

		Order saved = reload(order);
		assertThat(saved.getStatus()).isEqualTo(OrderStatus.COMPLETED);
		assertThat(saved.getCompletedAt()).isNotNull();
	}

	@Test
	void completedToCancelledIsRejectedWith409() throws Exception {
		Order order = saveOrder(7L, "10.00", OrderStatus.COMPLETED);

		patchStatus(order.getId(), "CANCELLED")
				.andExpect(status().isConflict());

		assertThat(reload(order).getStatus()).isEqualTo(OrderStatus.COMPLETED);
	}

	// --- helpers ---

	private Order saveOrder(Long ownerUserId, String total, OrderStatus status) {
		Order order = new Order(ownerUserId, new BigDecimal(total));
		order.setStatus(status);
		return orderRepository.save(order);
	}

	private Order reload(Order order) {
		return orderRepository.findById(order.getId()).orElseThrow();
	}

	private ResultActions patchStatus(Long id, String status) throws Exception {
		return mockMvc.perform(patch("/api/orders/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"status": "%s"}
						""".formatted(status)));
	}

}
