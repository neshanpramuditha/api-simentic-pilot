package com.example.semval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.semval.Finding.Outcome;
import com.example.semval.rules.ResponseInvariantRule;
import com.example.semval.rules.StateTransitionRule;
import com.fasterxml.jackson.databind.JsonNode;

class RuleEvaluationTests {

	private static JsonNode json(String text) {
		return Json.parseOrNull(text);
	}

	private static Exchange exchange(int index, String operationId, String method, Map<String, String> params, String request, int status, String response) {
		return new Exchange(index, operationId, method, "/x", params, json(request), status, json(response), 0);
	}

	private static final JsonNode IFF = json("""
			{"iff": [{"path": "$.completedAt", "notNull": true}, {"path": "$.status", "equals": "COMPLETED"}]}""");

	@Test
	void iffConditionCoversAllFourCombinations() {
		assertTrue(Condition.test(IFF, json("{\"status\":\"COMPLETED\",\"completedAt\":\"t\"}")));
		assertTrue(Condition.test(IFF, json("{\"status\":\"PAID\",\"completedAt\":null}")));
		assertFalse(Condition.test(IFF, json("{\"status\":\"CANCELLED\",\"completedAt\":\"t\"}")));
		assertFalse(Condition.test(IFF, json("{\"status\":\"COMPLETED\",\"completedAt\":null}")));
	}

	@Test
	void invariantChecksEveryItemOfAListAndSkipsErrors() {
		ResponseInvariantRule rule = new ResponseInvariantRule(json("""
				{"id": "INV", "description": "d", "operations": ["list"], "assert": %s}""".formatted(IFF)));
		List<Finding> list = rule.evaluate(exchange(0, "list", "GET", Map.of(), null, 200,
				"[{\"status\":\"PAID\",\"completedAt\":null},{\"status\":\"CANCELLED\",\"completedAt\":\"t\"}]"), null, null);
		assertEquals(List.of(Outcome.PASS, Outcome.FAIL), list.stream().map(Finding::outcome).toList());
		assertEquals(1, list.get(1).evidence().get("itemIndex").asInt());

		List<Finding> error = rule.evaluate(exchange(1, "list", "GET", Map.of(), null, 404, "{}"), null, null);
		assertEquals(Outcome.NOT_APPLICABLE, error.get(0).outcome());
	}

	@Test
	void passiveTransitionRuleUsesLastObservedStateAndFlagsAcceptedForbiddenTransition() {
		StateTransitionRule rule = new StateTransitionRule(json("""
				{"id": "ST", "description": "d", "operations": ["update"],
				 "resource": {"idParam": "id", "idField": "$.id", "stateField": "$.status", "readOperation": "get"},
				 "requestedState": "$request.body.status",
				 "transitions": {"CREATED": ["PAID"], "PAID": [], "COMPLETED": [], "CANCELLED": []},
				 "rejectedStatuses": [409]}"""));
		Exchange unknown = exchange(0, "update", "PATCH", Map.of("id", "1"), "{\"status\":\"CANCELLED\"}", 200, "{\"id\":1,\"status\":\"CANCELLED\"}");
		assertEquals(Outcome.NOT_APPLICABLE, rule.evaluate(unknown, null, null).get(0).outcome());

		rule.observe(exchange(1, "get", "GET", Map.of("id", "2"), null, 200, "{\"id\":2,\"status\":\"COMPLETED\"}"));
		Exchange accepted = exchange(2, "update", "PATCH", Map.of("id", "2"), "{\"status\":\"CANCELLED\"}", 200, "{\"id\":2,\"status\":\"CANCELLED\"}");
		Finding finding = rule.evaluate(accepted, null, null).get(0);
		assertEquals(Outcome.FAIL, finding.outcome());
		assertEquals("COMPLETED", finding.evidence().get("state").get("previous").asText());

		Exchange rejected = exchange(3, "update", "PATCH", Map.of("id", "2"), "{\"status\":\"PAID\"}", 409, "{}");
		assertEquals(Outcome.PASS, rule.evaluate(rejected, null, null).get(0).outcome());

		// Regression (evaluation matrix, clean Schemathesis traffic): 415 and 500 on a forbidden transition are not
		// evidence about the state machine and must not fail the rule.
		for (int status : new int[] { 400, 415, 500 }) {
			Exchange refused = exchange(4, "update", "PATCH", Map.of("id", "2"), "{\"status\":\"PAID\"}", status, "{}");
			assertEquals(Outcome.NOT_APPLICABLE, rule.evaluate(refused, null, null).get(0).outcome(), "HTTP " + status);
		}
		rule.observe(exchange(5, "get", "GET", Map.of("id", "3"), null, 200, "{\"id\":3,\"status\":\"CREATED\"}"));
		Exchange allowedButRejected = exchange(6, "update", "PATCH", Map.of("id", "3"), "{\"status\":\"PAID\"}", 409, "{}");
		assertEquals(Outcome.FAIL, rule.evaluate(allowedButRejected, null, null).get(0).outcome());
		Exchange allowedButUnsupported = exchange(7, "update", "PATCH", Map.of("id", "3"), "{\"status\":\"PAID\"}", 415, "{}");
		assertEquals(Outcome.NOT_APPLICABLE, rule.evaluate(allowedButUnsupported, null, null).get(0).outcome());
	}

	// 0.2: found with the Event Registration API, whose list operation returns {"count": n, "items": [...]}.
	@Test
	void envelopeWithoutItemsPathIsNotApplicableInsteadOfAVacuousPass() {
		ResponseInvariantRule plain = new ResponseInvariantRule(json("""
				{"id": "INV", "description": "d", "operations": ["list"], "assert": %s}""".formatted(IFF)));
		String envelope = "{\"count\":1,\"items\":[{\"status\":\"CANCELLED\",\"completedAt\":\"t\"}]}";
		List<Finding> vacuous = plain.evaluate(exchange(0, "list", "GET", Map.of(), null, 200, envelope), null, null);
		assertEquals(List.of(Outcome.NOT_APPLICABLE), vacuous.stream().map(Finding::outcome).toList());

		ResponseInvariantRule items = new ResponseInvariantRule(json("""
				{"id": "INV", "description": "d", "operations": ["list", "get"], "assert": %s, "items": {"list": "$.items"}}""".formatted(IFF)));
		List<Finding> checked = items.evaluate(exchange(1, "list", "GET", Map.of(), null, 200, envelope), null, null);
		assertEquals(List.of(Outcome.FAIL), checked.stream().map(Finding::outcome).toList());
		assertEquals(0, checked.get(0).evidence().get("itemIndex").asInt());
		assertEquals(Outcome.PASS, items.evaluate(exchange(2, "get", "GET", Map.of(), null, 200, "{\"status\":\"PAID\"}"), null, null).get(0).outcome());
		assertEquals(Outcome.NOT_APPLICABLE, items.evaluate(exchange(3, "list", "GET", Map.of(), null, 200, "{\"count\":0}"), null, null).get(0).outcome());
	}

	@Test
	void notAfterOrdersInstantsAndNumbersAndIsFalseWhenEitherSideIsMissing() {
		JsonNode order = json("{\"path\": \"$.a\", \"notAfter\": \"$.b\"}");
		assertTrue(Condition.test(order, json("{\"a\":\"2026-10-19T07:00:00Z\",\"b\":\"2026-10-19T07:00:00.000001Z\"}")));
		assertTrue(Condition.test(order, json("{\"a\":\"2026-10-19T09:00:00+02:00\",\"b\":\"2026-10-19T07:00:00Z\"}")));
		assertFalse(Condition.test(order, json("{\"a\":\"2026-10-19T07:00:01Z\",\"b\":\"2026-10-19T07:00:00Z\"}")));
		assertTrue(Condition.test(order, json("{\"a\":1,\"b\":2}")));
		assertFalse(Condition.test(order, json("{\"a\":\"2026-10-19T07:00:00Z\",\"b\":null}")));
		assertFalse(Condition.test(order, json("{\"a\":\"x\",\"b\":\"y\"}")));
		assertEquals(List.of("$.a", "$.b"), Condition.paths(order));
	}

}
