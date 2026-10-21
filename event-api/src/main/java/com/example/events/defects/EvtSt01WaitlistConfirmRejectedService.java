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

/**
 * INTENTIONAL DEFECT EVT-ST-01 (docs/experiments/event-reuse/defects.md): the allowed transition
 * {@code WAITLISTED -> CONFIRMED} is refused with 422 (an over-restrictive state machine). Nothing is stored.
 * Off by default; only exists when {@code events.defects.st01-waitlist-confirm-rejected.enabled=true}.
 */
@Service
@Primary
@ConditionalOnProperty(name = "events.defects.st01-waitlist-confirm-rejected.enabled", havingValue = "true")
public class EvtSt01WaitlistConfirmRejectedService extends RegistrationService {

	private static final Logger log = LoggerFactory.getLogger(EvtSt01WaitlistConfirmRejectedService.class);

	public EvtSt01WaitlistConfirmRejectedService(RegistrationStore store) {
		super(store);
		log.warn("DEFECT EVT-ST-01 ENABLED: WAITLISTED -> CONFIRMED is rejected with 422");
	}

	@Override
	@Transactional
	public Registration transition(String registrationId, TransitionRequest request) {
		Registration registration = fetch(registrationId);
		if (registration.getState() == RegistrationState.WAITLISTED && request.to() == RegistrationState.CONFIRMED) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Transition not allowed: WAITLISTED -> CONFIRMED");
		}
		return super.transition(registrationId, request);
	}

}
