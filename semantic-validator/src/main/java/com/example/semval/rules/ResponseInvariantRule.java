package com.example.semval.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.semval.Condition;
import com.example.semval.EvaluationContext;
import com.example.semval.Exchange;
import com.example.semval.Finding;
import com.example.semval.Finding.Outcome;
import com.example.semval.Json;
import com.example.semval.Rule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Cross-field invariant over successful response bodies. Array bodies are checked item by item.
 * <pre>
 * {"id": "...", "kind": "response-invariant", "operations": [...], "description": "...", "assert": &lt;condition&gt;,
 *  "items": {"listOperationId": "$.items"}}
 * </pre>
 * {@code items} (optional) names, per operation, the path of the array to check inside an envelope response.
 * A subject in which none of the fields the assertion reads is present is NOT_APPLICABLE, not a (vacuous) PASS.
 */
public final class ResponseInvariantRule implements Rule {

	private final String id;
	private final String description;
	private final List<String> operations;
	private final JsonNode assertion;
	private final Map<String, String> itemPaths = new HashMap<>();

	public ResponseInvariantRule(JsonNode definition) {
		this.id = definition.get("id").asText();
		this.description = definition.get("description").asText();
		this.operations = new ArrayList<>();
		definition.get("operations").forEach(op -> operations.add(op.asText()));
		this.assertion = definition.get("assert");
		definition.path("items").properties().forEach(e -> itemPaths.put(e.getKey(), e.getValue().asText()));
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
	public List<Finding> evaluate(Exchange exchange, JsonNode before, EvaluationContext context) {
		if (!exchange.isSuccess() || Json.isAbsentOrNull(exchange.responseBody())) {
			return List.of(new Finding(id, Outcome.NOT_APPLICABLE, exchange.index(), exchange.operationId(),
					"not a successful response with a body (HTTP " + exchange.status() + ")", null));
		}
		JsonNode body = exchange.responseBody();
		String itemPath = itemPaths.get(exchange.operationId());
		if (itemPath != null) {
			body = Json.at(body, itemPath);
			if (!body.isArray()) {
				return List.of(new Finding(id, Outcome.NOT_APPLICABLE, exchange.index(), exchange.operationId(),
						"no array at " + itemPath + " in the response", null));
			}
		}
		List<JsonNode> subjects = new ArrayList<>();
		if (body.isArray()) {
			body.forEach(subjects::add);
		} else {
			subjects.add(body);
		}
		List<Finding> findings = new ArrayList<>();
		List<String> paths = Condition.paths(assertion);
		for (int i = 0; i < subjects.size(); i++) {
			JsonNode subject = subjects.get(i);
			if (paths.stream().allMatch(p -> Json.at(subject, p).isMissingNode())) {
				findings.add(new Finding(id, Outcome.NOT_APPLICABLE, exchange.index(), exchange.operationId(),
						"none of the fields " + paths + " is present" + (body.isArray() ? " in item " + i : ""), null));
				continue;
			}
			if (Condition.test(assertion, subject)) {
				findings.add(new Finding(id, Outcome.PASS, exchange.index(), exchange.operationId(), "invariant holds", null));
				continue;
			}
			ObjectNode evidence = Evidence.of(exchange);
			ObjectNode values = evidence.putObject("values");
			for (String path : paths) {
				values.set(path, Json.at(subject, path).isMissingNode() ? Json.MAPPER.nullNode() : Json.at(subject, path));
			}
			if (body.isArray()) {
				evidence.put("itemIndex", i);
			}
			evidence.set("assertion", assertion);
			findings.add(new Finding(id, Outcome.FAIL, exchange.index(), exchange.operationId(),
					description + " — violated" + (body.isArray() ? " by item " + i : "") + ": " + values, evidence));
		}
		return findings;
	}

}
