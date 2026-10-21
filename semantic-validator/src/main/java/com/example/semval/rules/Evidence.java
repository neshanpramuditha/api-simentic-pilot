package com.example.semval.rules;

import com.example.semval.Exchange;
import com.example.semval.Json;
import com.fasterxml.jackson.databind.node.ObjectNode;

final class Evidence {

	private Evidence() {
	}

	/** The request and response that a finding refers to. */
	static ObjectNode of(Exchange exchange) {
		ObjectNode evidence = Json.MAPPER.createObjectNode();
		ObjectNode request = evidence.putObject("request");
		request.put("method", exchange.method());
		request.put("url", exchange.url());
		request.set("body", exchange.requestBody());
		ObjectNode response = evidence.putObject("response");
		response.put("status", exchange.status());
		response.set("body", exchange.responseBody());
		return evidence;
	}

}
