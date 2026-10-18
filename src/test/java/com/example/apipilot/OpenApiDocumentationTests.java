package com.example.apipilot;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:semantictestdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class OpenApiDocumentationTests {

	private static final String[] STATUSES = { "CREATED", "PAID", "SHIPPED", "DELIVERED", "COMPLETED", "CANCELLED" };

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiDocsIsAnOpenApi3DocumentWithTheFourEndpoints() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.openapi", startsWith("3.")))
				.andExpect(jsonPath("$.info.title", is("Order Management API")))
				.andExpect(jsonPath("$.paths", aMapWithSize(4)))
				.andExpect(jsonPath("$.paths['/api/orders']", hasKey("post")))
				.andExpect(jsonPath("$.paths['/api/orders/{id}']", hasKey("get")))
				.andExpect(jsonPath("$.paths['/api/users/{userId}/orders']", hasKey("get")))
				.andExpect(jsonPath("$.paths['/api/orders/{id}/status']", hasKey("patch")));
	}

	@Test
	void apiDocsDocumentsResponseCodes() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(jsonPath("$.paths['/api/orders'].post.responses", hasKey("201")))
				.andExpect(jsonPath("$.paths['/api/orders'].post.responses", hasKey("400")))
				.andExpect(jsonPath("$.paths['/api/orders/{id}'].get.responses", hasKey("404")))
				.andExpect(jsonPath("$.paths['/api/orders/{id}/status'].patch.responses", hasKey("409")))
				.andExpect(jsonPath("$.paths['/api/orders/{id}'].get.responses['200'].content['application/json'].schema['$ref']",
						is("#/components/schemas/Order")));
	}

	@Test
	void apiDocsDocumentsSchemasEnumsAndConstraints() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(jsonPath("$.components.schemas.CreateOrderRequest.required",
						containsInAnyOrder("ownerUserId", "total")))
				.andExpect(jsonPath("$.components.schemas.CreateOrderRequest.properties.ownerUserId.exclusiveMinimum", is(0)))
				.andExpect(jsonPath("$.components.schemas.CreateOrderRequest.properties.total.exclusiveMinimum", is(0.0)))
				.andExpect(jsonPath("$.components.schemas.UpdateOrderStatusRequest.required", containsInAnyOrder("status")))
				.andExpect(jsonPath("$.components.schemas.UpdateOrderStatusRequest.properties.status.enum",
						containsInAnyOrder((Object[]) STATUSES)))
				.andExpect(jsonPath("$.components.schemas.Order.properties.status.enum",
						containsInAnyOrder((Object[]) STATUSES)))
				.andExpect(jsonPath("$.components.schemas.Order.required",
						hasItems("id", "ownerUserId", "status", "total")));
	}

	@Test
	void swaggerUiIsServed() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("swagger-ui")));
	}

}
