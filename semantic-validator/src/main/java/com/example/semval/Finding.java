package com.example.semval;

import com.fasterxml.jackson.databind.node.ObjectNode;

/** The result of evaluating one rule against one exchange. FAIL findings carry the evidence needed to diagnose them. */
public record Finding(String ruleId, Outcome outcome, int exchangeIndex, String operationId, String message, ObjectNode evidence) {

	public enum Outcome {
		PASS, FAIL, NOT_APPLICABLE
	}

}
