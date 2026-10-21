package com.example.events.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request body for registering an attendee for an event")
public record RegistrationRequest(

		@Schema(description = "Reference of the attendee", example = "ATT-7")
		@NotNull
		@Pattern(regexp = "^ATT-[0-9]{1,8}$")
		String attendeeRef,

		@Schema(description = "Code of the event", example = "JAVA-CONF-26")
		@NotNull
		@Pattern(regexp = "^[A-Z0-9-]{3,20}$")
		String eventCode,

		@Schema(description = "Number of seats to reserve", example = "2")
		@NotNull
		@Min(1)
		@Max(4)
		Integer seats) {
}
