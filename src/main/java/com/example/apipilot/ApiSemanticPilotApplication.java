package com.example.apipilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(
		title = "Order Management API",
		version = "0.0.1",
		description = "Feasibility pilot for the API Contract-Drift and Semantic-Response Validation Framework. "
				+ "Manages orders and enforces a fixed order-status lifecycle."))
@SpringBootApplication
public class ApiSemanticPilotApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiSemanticPilotApplication.class, args);
	}

}
