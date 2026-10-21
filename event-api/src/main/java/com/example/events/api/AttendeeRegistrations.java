package com.example.events.api;

import java.util.List;

import com.example.events.model.Registration;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "All registrations of one attendee, oldest request first")
public record AttendeeRegistrations(

		@Schema(description = "Reference of the attendee", example = "ATT-7", requiredMode = Schema.RequiredMode.REQUIRED)
		String attendeeRef,

		@Schema(description = "Number of items", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
		int count,

		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		List<Registration> items) {
}
