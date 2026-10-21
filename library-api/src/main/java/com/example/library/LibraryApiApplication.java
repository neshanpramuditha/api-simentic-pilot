package com.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(
		title = "Library Lending API",
		version = "0.0.1",
		description = "Second subject API for the semantic-validator reuse experiment. "
				+ "Members request book loans, which are approved or rejected, issued and returned."))
@SpringBootApplication
public class LibraryApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
