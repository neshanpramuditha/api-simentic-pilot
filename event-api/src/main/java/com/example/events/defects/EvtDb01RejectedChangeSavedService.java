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
import com.example.events.service.RegistrationService;
import com.example.events.store.RegistrationStore;

/**
 * INTENTIONAL DEFECT EVT-DB-01 (docs/experiments/event-reuse/defects.md): a transition the unchanged service rejects
 * with 422 still returns 422, but the requested state (with its timestamp and reason) is stored. The accept/reject
 * decision is the clean service's. Off by default; only exists when
 * {@code events.defects.db01-rejected-change-saved.enabled=true}.
 */
@Service
@Primary
@ConditionalOnProperty(name = "events.defects.db01-rejected-change-saved.enabled", havingValue = "true")
public class EvtDb01RejectedChangeSavedService extends RegistrationService {

	private static final Logger log = LoggerFactory.getLogger(EvtDb01RejectedChangeSavedService.class);

	public EvtDb01RejectedChangeSavedService(RegistrationStore store) {
		super(store);
		log.warn("DEFECT EVT-DB-01 ENABLED: transitions rejected with 422 are stored anyway");
	}

	@Override
	@Transactional(noRollbackFor = ResponseStatusException.class)
	public Registration transition(String registrationId, TransitionRequest request) {
		try {
			return super.transition(registrationId, request);
		} catch (ResponseStatusException rejected) {
			if (rejected.getStatusCode() == HttpStatus.UNPROCESSABLE_CONTENT) {
				Registration registration = fetch(registrationId);
				apply(registration, request.to(), request.reason());
				store.save(registration);
			}
			throw rejected;
		}
	}

}
