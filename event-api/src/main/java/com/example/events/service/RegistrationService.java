package com.example.events.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.events.api.RegistrationRequest;
import com.example.events.api.TransitionRequest;
import com.example.events.model.Registration;
import com.example.events.model.RegistrationState;
import com.example.events.store.RegistrationStore;

@Service
public class RegistrationService {

	// Allowed state changes; every other pair (including staying in the same state) is 422 and changes nothing.
	private static final Map<RegistrationState, Set<RegistrationState>> LIFECYCLE = new EnumMap<>(RegistrationState.class);

	static {
		LIFECYCLE.put(RegistrationState.REQUESTED,
				EnumSet.of(RegistrationState.CONFIRMED, RegistrationState.WAITLISTED, RegistrationState.CANCELLED));
		LIFECYCLE.put(RegistrationState.WAITLISTED, EnumSet.of(RegistrationState.CONFIRMED, RegistrationState.CANCELLED));
		LIFECYCLE.put(RegistrationState.CONFIRMED, EnumSet.of(RegistrationState.CHECKED_IN, RegistrationState.CANCELLED));
		LIFECYCLE.put(RegistrationState.CHECKED_IN, EnumSet.noneOf(RegistrationState.class));
		LIFECYCLE.put(RegistrationState.CANCELLED, EnumSet.noneOf(RegistrationState.class));
	}

	private static final String ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

	private final SecureRandom random = new SecureRandom();

	protected final RegistrationStore store;

	public RegistrationService(RegistrationStore store) {
		this.store = store;
	}

	public static boolean isAllowed(RegistrationState from, RegistrationState to) {
		return LIFECYCLE.get(from).contains(to);
	}

	@Transactional
	public Registration register(RegistrationRequest request) {
		String id;
		do {
			StringBuilder sb = new StringBuilder("REG-");
			for (int i = 0; i < 8; i++) {
				sb.append(ID_ALPHABET.charAt(random.nextInt(ID_ALPHABET.length())));
			}
			id = sb.toString();
		} while (store.existsById(id));
		return store.save(new Registration(id, request.attendeeRef(), request.eventCode(), request.seats()));
	}

	@Transactional(readOnly = true)
	public Registration fetch(String registrationId) {
		return store.findById(registrationId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration " + registrationId + " not found"));
	}

	@Transactional(readOnly = true)
	public List<Registration> listForAttendee(String attendeeRef) {
		return store.findByAttendeeRefOrderByRequestedAtAscRegistrationIdAsc(attendeeRef);
	}

	@Transactional
	public Registration transition(String registrationId, TransitionRequest request) {
		Registration registration = fetch(registrationId);
		RegistrationState from = registration.getState();
		RegistrationState to = request.to();
		if (to == RegistrationState.CANCELLED && (request.reason() == null || request.reason().isBlank())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A reason is required to cancel a registration");
		}
		if (!isAllowed(from, to)) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Transition not allowed: " + from + " -> " + to);
		}
		apply(registration, to, request.reason());
		return store.save(registration);
	}

	/** Moves the registration to {@code to} and records the timestamp (and reason) that state carries. */
	protected void apply(Registration registration, RegistrationState to, String reason) {
		Instant now = Instant.now();
		registration.setState(to);
		switch (to) {
			case CONFIRMED -> registration.setConfirmedAt(now);
			case CHECKED_IN -> registration.setCheckedInAt(now);
			case CANCELLED -> {
				registration.setCancelledAt(now);
				registration.setCancellationReason(reason);
			}
			default -> {
				// REQUESTED and WAITLISTED carry no timestamp
			}
		}
	}

}
