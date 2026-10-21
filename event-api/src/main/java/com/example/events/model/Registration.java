package com.example.events.model;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Schema(description = "An attendee's registration for an event")
@Entity
@Table(name = "event_registration")
public class Registration {

	@Schema(description = "Registration identifier, assigned by the server", example = "REG-7QK2M9XD",
			pattern = "^REG-[A-Z0-9]{8}$", accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
	@Id
	@Column(name = "registration_id", length = 12)
	private String registrationId;

	@Schema(description = "Reference of the attendee", example = "ATT-7", requiredMode = Schema.RequiredMode.REQUIRED)
	@Column(nullable = false, length = 12)
	private String attendeeRef;

	@Schema(description = "Code of the event", example = "JAVA-CONF-26", requiredMode = Schema.RequiredMode.REQUIRED)
	@Column(nullable = false, length = 20)
	private String eventCode;

	@Schema(description = "Number of seats reserved", example = "2", minimum = "1", maximum = "4",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@Column(nullable = false)
	private int seats;

	@Schema(description = "Lifecycle state", example = "REQUESTED", requiredMode = Schema.RequiredMode.REQUIRED)
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 12)
	private RegistrationState state;

	@Schema(description = "When the registration was requested", example = "2026-10-19T09:00:00.000001Z",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@Column(nullable = false)
	private Instant requestedAt;

	@Schema(description = "When the registration was confirmed; kept after check-in or cancellation; null if never confirmed",
			implementation = String.class, format = "date-time", types = { "string", "null" },
			requiredMode = Schema.RequiredMode.REQUIRED)
	private Instant confirmedAt;

	@Schema(description = "When the attendee checked in; null unless CHECKED_IN", implementation = String.class,
			format = "date-time", types = { "string", "null" }, requiredMode = Schema.RequiredMode.REQUIRED)
	private Instant checkedInAt;

	@Schema(description = "When the registration was cancelled; null unless CANCELLED", implementation = String.class,
			format = "date-time", types = { "string", "null" }, requiredMode = Schema.RequiredMode.REQUIRED)
	private Instant cancelledAt;

	@Schema(description = "Reason given for the cancellation; null unless CANCELLED", implementation = String.class,
			types = { "string", "null" }, maxLength = 200, requiredMode = Schema.RequiredMode.REQUIRED)
	@Column(length = 200)
	private String cancellationReason;

	protected Registration() {
		// required by JPA
	}

	public Registration(String registrationId, String attendeeRef, String eventCode, int seats) {
		this.registrationId = registrationId;
		this.attendeeRef = attendeeRef;
		this.eventCode = eventCode;
		this.seats = seats;
		this.state = RegistrationState.REQUESTED;
		this.requestedAt = Instant.now();
	}

	public String getRegistrationId() {
		return registrationId;
	}

	public String getAttendeeRef() {
		return attendeeRef;
	}

	public String getEventCode() {
		return eventCode;
	}

	public int getSeats() {
		return seats;
	}

	public RegistrationState getState() {
		return state;
	}

	public void setState(RegistrationState state) {
		this.state = state;
	}

	public Instant getRequestedAt() {
		return requestedAt;
	}

	public Instant getConfirmedAt() {
		return confirmedAt;
	}

	public void setConfirmedAt(Instant confirmedAt) {
		this.confirmedAt = confirmedAt;
	}

	public Instant getCheckedInAt() {
		return checkedInAt;
	}

	public void setCheckedInAt(Instant checkedInAt) {
		this.checkedInAt = checkedInAt;
	}

	public Instant getCancelledAt() {
		return cancelledAt;
	}

	public void setCancelledAt(Instant cancelledAt) {
		this.cancelledAt = cancelledAt;
	}

	public String getCancellationReason() {
		return cancellationReason;
	}

	public void setCancellationReason(String cancellationReason) {
		this.cancellationReason = cancellationReason;
	}

}
