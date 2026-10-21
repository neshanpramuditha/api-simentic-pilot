package com.example.events.defects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.example.events.model.Registration;
import com.example.events.model.RegistrationState;
import com.example.events.service.RegistrationService;
import com.example.events.store.RegistrationStore;

/**
 * INTENTIONAL DEFECT EVT-CF-01 (docs/experiments/event-reuse/defects.md): the check-in is accepted and stored with a
 * {@code checkedInAt}, but the value is taken from {@code requestedAt}, so the attendee appears to have checked in
 * before the registration was confirmed (time-order invariant). Off by default; only exists when
 * {@code events.defects.cf01-checkin-before-confirmation.enabled=true}.
 */
@Service
@Primary
@ConditionalOnProperty(name = "events.defects.cf01-checkin-before-confirmation.enabled", havingValue = "true")
public class EvtCf01CheckInBeforeConfirmationService extends RegistrationService {

	private static final Logger log = LoggerFactory.getLogger(EvtCf01CheckInBeforeConfirmationService.class);

	public EvtCf01CheckInBeforeConfirmationService(RegistrationStore store) {
		super(store);
		log.warn("DEFECT EVT-CF-01 ENABLED: checkedInAt is set to requestedAt (before confirmedAt)");
	}

	@Override
	protected void apply(Registration registration, RegistrationState to, String reason) {
		super.apply(registration, to, reason);
		if (to == RegistrationState.CHECKED_IN) {
			registration.setCheckedInAt(registration.getRequestedAt());
		}
	}

}
