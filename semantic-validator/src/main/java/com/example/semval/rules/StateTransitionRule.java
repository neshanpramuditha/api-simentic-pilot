package com.example.semval.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.semval.EvaluationContext;
import com.example.semval.Exchange;
import com.example.semval.Finding;
import com.example.semval.Finding.Outcome;
import com.example.semval.Json;
import com.example.semval.Rule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * State-machine constraint for an operation that changes a resource's state.
 * <pre>
 * {"id": "...", "kind": "state-transition", "operations": ["updateStatus"], "description": "...",
 *  "resource": {"idParam": "id", "idField": "$.id", "stateField": "$.status", "readOperation": "getOrder"},
 *  "requestedState": "$request.body.status",
 *  "transitions": {"CREATED": ["PAID", "CANCELLED"], ...},
 *  "rejectedStatuses": [409]}
 * </pre>
 * The previous state comes from a read-back before the request (active mode) or from the last state observed for the
 * same resource id in earlier responses (passive mode). Allowed transitions must succeed and return the requested
 * state; forbidden transitions must not succeed. A response with one of {@code rejectedStatuses} is the business
 * rejection; any other error status is outside the rule (NOT_APPLICABLE).
 */
public final class StateTransitionRule implements Rule {

	private final String id;
	private final String description;
	private final List<String> operations = new ArrayList<>();
	private final String idParam;
	private final String idField;
	private final String stateField;
	private final String readOperation;
	private final String requestedState;
	private final Map<String, Set<String>> transitions = new LinkedHashMap<>();
	private final Set<Integer> rejectedStatuses = new LinkedHashSet<>();
	private final Map<String, String> observedStates = new HashMap<>();

	public StateTransitionRule(JsonNode definition) {
		this.id = definition.get("id").asText();
		this.description = definition.get("description").asText();
		definition.get("operations").forEach(op -> operations.add(op.asText()));
		JsonNode resource = definition.get("resource");
		this.idParam = resource.get("idParam").asText();
		this.idField = resource.get("idField").asText();
		this.stateField = resource.get("stateField").asText();
		this.readOperation = resource.get("readOperation").asText();
		this.requestedState = definition.get("requestedState").asText();
		definition.get("transitions").properties().forEach(e -> {
			Set<String> targets = new LinkedHashSet<>();
			e.getValue().forEach(t -> targets.add(t.asText()));
			transitions.put(e.getKey(), targets);
		});
		definition.get("rejectedStatuses").forEach(s -> rejectedStatuses.add(s.asInt()));
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String description() {
		return description;
	}

	@Override
	public List<String> operations() {
		return operations;
	}

	@Override
	public JsonNode before(EvaluationContext context, String operationId, Map<String, String> pathParams, JsonNode requestBody) {
		String resourceId = pathParams.get(idParam);
		return resourceId == null ? null : context.read(readOperation, Map.of(idParam, resourceId));
	}

	@Override
	public List<Finding> evaluate(Exchange exchange, JsonNode before, EvaluationContext context) {
		String resourceId = exchange.pathParams().get(idParam);
		String target = Json.text(exchange.resolve(requestedState));
		String previous;
		String source;
		if (before != null) {
			if (before.path("status").asInt() != 200) {
				return na(exchange, "resource " + resourceId + " not readable before the request (HTTP " + before.path("status").asInt() + ")");
			}
			previous = Json.text(Json.at(before.path("body"), stateField));
			source = "read-back before request";
		} else {
			previous = observedStates.get(resourceId);
			source = "last state observed in earlier responses";
		}
		if (previous == null) {
			return na(exchange, "previous state of resource " + resourceId + " unknown");
		}
		if (target == null || !transitions.containsKey(target)) {
			return na(exchange, "requested state is not a known state: " + target);
		}
		boolean allowed = transitions.getOrDefault(previous, Set.of()).contains(target);

		ObjectNode evidence = Evidence.of(exchange);
		ObjectNode state = evidence.putObject("state");
		state.put("resourceId", resourceId);
		state.put("previous", previous);
		state.put("previousSource", source);
		if (before != null) {
			state.set("readBack", before);
		}
		state.put("requested", target);
		state.put("transitionAllowed", allowed);
		state.set("allowedFromPrevious", Json.MAPPER.valueToTree(transitions.getOrDefault(previous, Set.of())));

		// Only 2xx and the business rejection statuses say anything about the transition; other errors (400, 415, 500...)
		// mean the request was refused before the state machine was consulted, so the rule does not apply to them.
		if (!exchange.isSuccess() && !rejectedStatuses.contains(exchange.status())) {
			return na(exchange, "request refused for a reason outside this rule (HTTP " + exchange.status() + ")");
		}
		if (allowed) {
			String returned = Json.text(Json.at(exchange.responseBody(), stateField));
			if (!exchange.isSuccess()) {
				return fail(exchange, "allowed transition " + previous + " -> " + target + " was rejected with HTTP " + exchange.status(), evidence);
			}
			if (!target.equals(returned)) {
				return fail(exchange, "allowed transition " + previous + " -> " + target + " returned state " + returned, evidence);
			}
			return pass(exchange, "allowed transition " + previous + " -> " + target + " succeeded");
		}
		if (exchange.isSuccess()) {
			return fail(exchange, "forbidden transition " + previous + " -> " + target + " was accepted with HTTP " + exchange.status()
					+ " (allowed from " + previous + ": " + transitions.getOrDefault(previous, Set.of()) + ")", evidence);
		}
		return pass(exchange, "forbidden transition " + previous + " -> " + target + " rejected with HTTP " + exchange.status());
	}

	@Override
	public void observe(Exchange exchange) {
		if (!exchange.isSuccess() || Json.isAbsentOrNull(exchange.responseBody())) {
			return;
		}
		JsonNode body = exchange.responseBody();
		for (JsonNode item : body.isArray() ? body : List.of(body)) {
			String resourceId = Json.text(Json.at(item, idField));
			String state = Json.text(Json.at(item, stateField));
			if (resourceId != null && state != null) {
				observedStates.put(resourceId, state);
			}
		}
	}

	private List<Finding> pass(Exchange exchange, String message) {
		return List.of(new Finding(id, Outcome.PASS, exchange.index(), exchange.operationId(), message, null));
	}

	private List<Finding> fail(Exchange exchange, String message, ObjectNode evidence) {
		return List.of(new Finding(id, Outcome.FAIL, exchange.index(), exchange.operationId(), message, evidence));
	}

	private List<Finding> na(Exchange exchange, String message) {
		return List.of(new Finding(id, Outcome.NOT_APPLICABLE, exchange.index(), exchange.operationId(), message, null));
	}

}
