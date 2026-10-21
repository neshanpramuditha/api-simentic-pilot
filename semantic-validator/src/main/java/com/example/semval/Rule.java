package com.example.semval;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/** A developer-defined semantic rule bound to OpenAPI operations by operationId. */
public interface Rule {

	String id();

	String description();

	/** OpenAPI operationIds this rule applies to. */
	List<String> operations();

	/**
	 * Active mode only: called before the request is sent, so the rule can capture the state it needs
	 * (e.g. the resource's current status or a database row). Returns {@code null} when nothing is needed.
	 */
	default JsonNode before(EvaluationContext context, String operationId, Map<String, String> pathParams, JsonNode requestBody) {
		return null;
	}

	/** Evaluates the rule; {@code before} is what {@link #before} returned (always {@code null} in passive mode). */
	List<Finding> evaluate(Exchange exchange, JsonNode before, EvaluationContext context);

	/** Called after every exchange (in order), so rules can track state observed in responses. */
	default void observe(Exchange exchange) {
	}

}
