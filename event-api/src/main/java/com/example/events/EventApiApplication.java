package com.example.events;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(
		title = "Event Registration API",
		version = "1.0.0",
		description = "Third subject API for the semantic-validator portability experiment. Attendees register for "
				+ "events; registrations are confirmed or wait-listed, checked in, or cancelled with a reason."))
@SpringBootApplication
public class EventApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventApiApplication.class, args);
	}

}
