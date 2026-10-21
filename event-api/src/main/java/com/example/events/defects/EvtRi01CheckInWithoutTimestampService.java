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
 * INTENTIONAL DEFECT EVT-RI-01 (docs/experiments/event-reuse/defects.md): the allowed check-in is accepted and stored,
 * but {@code checkedInAt} is not recorded (null in the response and the database). Off by default; only exists when
 * {@code events.defects.ri01-checkin-without-timestamp.enabled=true}.
 */
@Service
@Primary
@ConditionalOnProperty(name = "events.defects.ri01-checkin-without-timestamp.enabled", havingValue = "true")
public class EvtRi01CheckInWithoutTimestampService extends RegistrationService {

	private static final Logger log = LoggerFactory.getLogger(EvtRi01CheckInWithoutTimestampService.class);

	public EvtRi01CheckInWithoutTimestampService(RegistrationStore store) {
		super(store);
		log.warn("DEFECT EVT-RI-01 ENABLED: CHECKED_IN registrations are stored without checkedInAt");
	}

	@Override
	protected void apply(Registration registration, RegistrationState to, String reason) {
		super.apply(registration, to, reason);
		if (to == RegistrationState.CHECKED_IN) {
			registration.setCheckedInAt(null);
		}
	}

}
