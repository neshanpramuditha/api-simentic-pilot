package com.example.events.defects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.events.model.Registration;
import com.example.events.model.RegistrationState;
import com.example.events.store.RegistrationStore;

final class DefectSupport {

	private static int counter;

	private DefectSupport() {
	}

	/** Stores a registration directly in {@code state} (no timestamps), bypassing the service. */
	static Registration stored(RegistrationStore store, RegistrationState state) {
		Registration r = new Registration(String.format("REG-T%07d", ++counter), "ATT-7", "JAVA-CONF-26", 1);
		r.setState(state);
		return store.save(r);
	}

	static ResultActions transition(MockMvc mockMvc, String id, String to, String reason) throws Exception {
		String body = reason == null ? "{\"to\":\"" + to + "\"}" : "{\"to\":\"" + to + "\",\"reason\":\"" + reason + "\"}";
		return mockMvc.perform(post("/v1/registrations/{id}/transitions", id).contentType(MediaType.APPLICATION_JSON).content(body));
	}

	static ResultActions fetch(MockMvc mockMvc, String id) throws Exception {
		return mockMvc.perform(get("/v1/registrations/{id}", id));
	}

}
