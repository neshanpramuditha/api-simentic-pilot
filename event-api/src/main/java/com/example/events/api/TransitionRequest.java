package com.example.events.api;

import com.example.events.model.RegistrationState;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Requested state change")
public record TransitionRequest(

		@Schema(description = "Target state; must be allowed from the current state", example = "CONFIRMED")
		@NotNull
		RegistrationState to,

		@Schema(description = "Reason; required (non-blank) when the target is CANCELLED, ignored otherwise",
				example = "Schedule conflict", maxLength = 200)
		@Size(max = 200)
		String reason) {
}
