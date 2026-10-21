package com.example.semval.rules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.semval.EvaluationContext;
import com.example.semval.Exchange;
import com.example.semval.Finding;
import com.example.semval.Finding.Outcome;
import com.example.semval.Json;
import com.example.semval.Rule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Database-state consistency for a write operation, checked with a row snapshot before and after the request.
 * <pre>
 * {"id": "...", "kind": "db-consistency", "operations": ["updateStatus"], "description": "...",
 *  "query": "SELECT status, completed_at FROM orders WHERE id = ?", "params": ["$path.id"],
 *  "onSuccess": {"columnsMatchResponse": {"status": "$.status"}},
 *  "onError": "rowUnchanged"}
 * </pre>
 * After a 2xx response the listed columns must equal the response fields; after a 4xx response the row must be
 * exactly as it was before the request. Requires a database connection (active mode).
 */
public final class DatabaseConsistencyRule implements Rule {

	private final String id;
	private final String description;
	private final List<String> operations = new ArrayList<>();
	private final String query;
	private final List<String> params = new ArrayList<>();
	private final Map<String, String> columnsMatchResponse = new LinkedHashMap<>();
	private final boolean rowUnchangedOnError;

	public DatabaseConsistencyRule(JsonNode definition) {
		this.id = definition.get("id").asText();
		this.description = definition.get("description").asText();
		definition.get("operations").forEach(op -> operations.add(op.asText()));
		this.query = definition.get("query").asText();
		definition.get("params").forEach(p -> params.add(p.asText()));
		definition.path("onSuccess").path("columnsMatchResponse").properties()
				.forEach(e -> columnsMatchResponse.put(e.getKey(), e.getValue().asText()));
		this.rowUnchangedOnError = "rowUnchanged".equals(definition.path("onError").asText());
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
		if (!context.hasDatabase()) {
			return null;
		}
		ObjectNode snapshot = Json.MAPPER.createObjectNode();
		JsonNode row = context.queryRow(query, resolveParams(pathParams));
		snapshot.set("row", row == null ? context.jsonNull() : row);
		return snapshot;
	}

	@Override
	public List<Finding> evaluate(Exchange exchange, JsonNode before, EvaluationContext context) {
		if (!context.hasDatabase() || before == null) {
			return na(exchange, "no database access (passive mode or no JDBC URL configured)");
		}
		JsonNode rowBefore = before.get("row");
		if (rowBefore.isNull()) {
			return na(exchange, "no row before the request");
		}
		JsonNode rowAfter = context.queryRow(query, resolveParams(exchange.pathParams()));

		ObjectNode evidence = Evidence.of(exchange);
		ObjectNode db = evidence.putObject("database");
		db.put("query", query);
		db.set("params", Json.MAPPER.valueToTree(resolveParams(exchange.pathParams())));
		db.set("rowBefore", rowBefore);
		db.set("rowAfter", rowAfter == null ? context.jsonNull() : rowAfter);

		if (exchange.isSuccess()) {
			List<String> mismatches = new ArrayList<>();
			for (Map.Entry<String, String> e : columnsMatchResponse.entrySet()) {
				String persisted = rowAfter == null ? null : Json.text(rowAfter.get(e.getKey()));
				String returned = Json.text(Json.at(exchange.responseBody(), e.getValue()));
				if (persisted == null ? returned != null : !persisted.equals(returned)) {
					mismatches.add(e.getKey() + ": persisted=" + persisted + " response(" + e.getValue() + ")=" + returned);
				}
			}
			if (!mismatches.isEmpty()) {
				return fail(exchange, "persisted state differs from the " + exchange.status() + " response: " + mismatches, evidence);
			}
			return pass(exchange, "persisted state matches the response");
		}
		if (exchange.status() >= 400 && exchange.status() < 500 && rowUnchangedOnError) {
			if (!rowBefore.equals(rowAfter)) {
				return fail(exchange, "request was rejected with HTTP " + exchange.status() + " but the row changed: before "
						+ rowBefore + ", after " + rowAfter, evidence);
			}
			return pass(exchange, "row unchanged after HTTP " + exchange.status());
		}
		return na(exchange, "HTTP " + exchange.status() + " not covered by this rule");
	}

	private List<String> resolveParams(Map<String, String> pathParams) {
		List<String> values = new ArrayList<>();
		for (String p : params) {
			if (!p.startsWith("$path.")) {
				throw new IllegalArgumentException("only $path.* parameters are supported: " + p);
			}
			values.add(pathParams.get(p.substring("$path.".length())));
		}
		return values;
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
