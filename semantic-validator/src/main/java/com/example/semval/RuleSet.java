package com.example.semval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.semval.rules.DatabaseConsistencyRule;
import com.example.semval.rules.ResponseInvariantRule;
import com.example.semval.rules.StateTransitionRule;
import com.fasterxml.jackson.databind.JsonNode;

/** Loads rules from a JSON rules file and checks that every referenced operationId exists in the OpenAPI document. */
public final class RuleSet {

	private final String name;
	private final List<Rule> rules = new ArrayList<>();

	public RuleSet(JsonNode file, OpenApiIndex openApi) {
		this.name = file.path("name").asText("rules");
		Set<String> ids = new HashSet<>();
		for (JsonNode definition : file.get("rules")) {
			Rule rule = switch (definition.get("kind").asText()) {
				case "response-invariant" -> new ResponseInvariantRule(definition);
				case "state-transition" -> new StateTransitionRule(definition);
				case "db-consistency" -> new DatabaseConsistencyRule(definition);
				default -> throw new IllegalArgumentException("unknown rule kind: " + definition.get("kind"));
			};
			if (!ids.add(rule.id())) {
				throw new IllegalArgumentException("duplicate rule id: " + rule.id());
			}
			rule.operations().forEach(openApi::require);
			rules.add(rule);
		}
	}

	public String name() {
		return name;
	}

	public List<Rule> rules() {
		return rules;
	}

	public List<Rule> forOperation(String operationId) {
		return rules.stream().filter(r -> r.operations().contains(operationId)).toList();
	}

}
