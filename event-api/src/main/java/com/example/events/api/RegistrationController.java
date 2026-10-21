package com.example.events.api;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.events.model.Registration;
import com.example.events.service.RegistrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Registrations", description = "Register attendees for events and move registrations through their lifecycle")
@RestController
@RequestMapping("/v1")
public class RegistrationController {

	private final RegistrationService service;

	public RegistrationController(RegistrationService service) {
		this.service = service;
	}

	@Operation(operationId = "registerAttendee", summary = "Register an attendee for an event",
			description = "Creates a registration in state REQUESTED.")
	@ApiResponse(responseCode = "201", description = "Registration created",
			headers = @Header(name = "Location", description = "URL of the new registration", schema = @Schema(type = "string")))
	@ApiResponse(responseCode = "400", description = "Request body failed validation", content = @Content)
	@PostMapping("/registrations")
	public ResponseEntity<Registration> registerAttendee(@Valid @RequestBody RegistrationRequest request) {
		Registration created = service.register(request);
		return ResponseEntity.created(URI.create("/v1/registrations/" + created.getRegistrationId())).body(created);
	}

	@Operation(operationId = "fetchRegistration", summary = "Fetch a registration")
	@ApiResponse(responseCode = "200", description = "Registration found")
	@ApiResponse(responseCode = "404", description = "No registration with this identifier", content = @Content)
	@GetMapping("/registrations/{registrationId}")
	public Registration fetchRegistration(
			@Parameter(description = "Registration identifier", example = "REG-7QK2M9XD") @PathVariable String registrationId) {
		return service.fetch(registrationId);
	}

	@Operation(operationId = "listAttendeeRegistrations", summary = "List an attendee's registrations",
			description = "Returns all registrations of the attendee, oldest request first, in an envelope.")
	@ApiResponse(responseCode = "200", description = "Registrations of the attendee (possibly none)")
	@GetMapping("/attendees/{attendeeRef}/registrations")
	public AttendeeRegistrations listAttendeeRegistrations(
			@Parameter(description = "Reference of the attendee", example = "ATT-7") @PathVariable String attendeeRef) {
		var items = service.listForAttendee(attendeeRef);
		return new AttendeeRegistrations(attendeeRef, items.size(), items);
	}

	@Operation(operationId = "transitionRegistration", summary = "Change a registration's state",
			description = "Allowed: REQUESTED -> CONFIRMED | WAITLISTED | CANCELLED; WAITLISTED -> CONFIRMED | CANCELLED; "
					+ "CONFIRMED -> CHECKED_IN | CANCELLED. CHECKED_IN and CANCELLED are final. Any other change is 422. "
					+ "CONFIRMED sets confirmedAt, CHECKED_IN sets checkedInAt, CANCELLED sets cancelledAt and "
					+ "cancellationReason (a non-blank reason is required).")
	@ApiResponse(responseCode = "200", description = "State changed")
	@ApiResponse(responseCode = "400", description = "Unknown target state, invalid body, or cancellation without a reason",
			content = @Content)
	@ApiResponse(responseCode = "404", description = "No registration with this identifier", content = @Content)
	@ApiResponse(responseCode = "422", description = "Transition not allowed from the current state", content = @Content)
	@PostMapping("/registrations/{registrationId}/transitions")
	@ResponseStatus(HttpStatus.OK)
	public Registration transitionRegistration(
			@Parameter(description = "Registration identifier", example = "REG-7QK2M9XD") @PathVariable String registrationId,
			@Valid @RequestBody TransitionRequest request) {
		return service.transition(registrationId, request);
	}

}
