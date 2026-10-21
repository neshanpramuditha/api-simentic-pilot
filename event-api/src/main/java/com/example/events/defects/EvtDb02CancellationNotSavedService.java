package com.example.events.defects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.events.api.TransitionRequest;
import com.example.events.model.Registration;
import com.example.events.model.RegistrationState;
import com.example.events.service.RegistrationService;
import com.example.events.store.RegistrationStore;

import jakarta.persistence.EntityManager;

/**
 * INTENTIONAL DEFECT EVT-DB-02 (docs/experiments/event-reuse/defects.md): an allowed cancellation returns 200 with
 * the cancelled registration (state, cancelledAt, reason), but nothing is stored: the entity is detached before the
 * change, so the database keeps the previous state (a lost write). Validation (400) and rejection (422) are the clean
 * service's. Off by default; only exists when {@code events.defects.db02-cancellation-not-saved.enabled=true}.
 */
@Service
@Primary
@ConditionalOnProperty(name = "events.defects.db02-cancellation-not-saved.enabled", havingValue = "true")
public class EvtDb02CancellationNotSavedService extends RegistrationService {

	private static final Logger log = LoggerFactory.getLogger(EvtDb02CancellationNotSavedService.class);

	private final EntityManager entityManager;

	public EvtDb02CancellationNotSavedService(RegistrationStore store, EntityManager entityManager) {
		super(store);
		this.entityManager = entityManager;
		log.warn("DEFECT EVT-DB-02 ENABLED: successful cancellations are returned but not stored");
	}

	@Override
	@Transactional
	public Registration transition(String registrationId, TransitionRequest request) {
		Registration registration = fetch(registrationId);
		boolean hasReason = request.reason() != null && !request.reason().isBlank();
		if (request.to() == RegistrationState.CANCELLED && hasReason && isAllowed(registration.getState(), RegistrationState.CANCELLED)) {
			entityManager.detach(registration);
			apply(registration, RegistrationState.CANCELLED, request.reason());
			return registration;
		}
		if (request.to() == RegistrationState.CANCELLED && !hasReason) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A reason is required to cancel a registration");
		}
		return super.transition(registrationId, request);
	}

}
