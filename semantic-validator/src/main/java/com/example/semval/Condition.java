package com.example.semval;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Declarative conditions over a JSON document.
 * <pre>
 * {"path": "$.a", "equals": v} | {"path": "$.a", "notNull": true} | {"path": "$.a", "isNull": true} | {"path": "$.a", "in": [..]}
 * {"path": "$.a", "notAfter": "$.b"}   both set and a &lt;= b (ISO-8601 date-times compared as instants, otherwise numbers)
 * {"iff": [c1, c2]} | {"implies": [c1, c2]} | {"all": [c..]} | {"any": [c..]} | {"not": c}
 * </pre>
 */
public final class Condition {

	private Condition() {
	}

	public static boolean test(JsonNode condition, JsonNode subject) {
		if (condition.has("iff")) {
			return test(condition.get("iff").get(0), subject) == test(condition.get("iff").get(1), subject);
		}
		if (condition.has("implies")) {
			return !test(condition.get("implies").get(0), subject) || test(condition.get("implies").get(1), subject);
		}
		if (condition.has("all")) {
			for (JsonNode c : condition.get("all")) {
				if (!test(c, subject)) {
					return false;
				}
			}
			return true;
		}
		if (condition.has("any")) {
			for (JsonNode c : condition.get("any")) {
				if (test(c, subject)) {
					return true;
				}
			}
			return false;
		}
		if (condition.has("not")) {
			return !test(condition.get("not"), subject);
		}
		JsonNode value = Json.at(subject, condition.get("path").asText());
		if (condition.has("equals")) {
			return !Json.isAbsentOrNull(value) && value.equals(condition.get("equals"));
		}
		if (condition.has("notNull")) {
			return !Json.isAbsentOrNull(value);
		}
		if (condition.has("isNull")) {
			return Json.isAbsentOrNull(value);
		}
		if (condition.has("in")) {
			for (JsonNode allowed : condition.get("in")) {
				if (allowed.equals(value)) {
					return true;
				}
			}
			return false;
		}
		if (condition.has("notAfter")) {
			JsonNode other = Json.at(subject, condition.get("notAfter").asText());
			if (Json.isAbsentOrNull(value) || Json.isAbsentOrNull(other)) {
				return false;
			}
			Integer order = compare(value, other);
			return order != null && order <= 0;
		}
		throw new IllegalArgumentException("unsupported condition: " + condition);
	}

	/** Orders two values as instants (ISO-8601 date-times) or numbers; null if they are not comparable. */
	private static Integer compare(JsonNode a, JsonNode b) {
		if (a.isNumber() && b.isNumber()) {
			return a.decimalValue().compareTo(b.decimalValue());
		}
		try {
			return OffsetDateTime.parse(a.asText()).toInstant().compareTo(OffsetDateTime.parse(b.asText()).toInstant());
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	/** Lists the paths a condition reads, for evidence. */
	public static List<String> paths(JsonNode condition) {
		List<String> paths = new ArrayList<>();
		if (condition.has("path")) {
			paths.add(condition.get("path").asText());
		}
		if (condition.has("notAfter")) {
			paths.add(condition.get("notAfter").asText());
		}
		for (String key : List.of("iff", "implies", "all", "any")) {
			if (condition.has(key)) {
				condition.get(key).forEach(c -> paths.addAll(paths(c)));
			}
		}
		if (condition.has("not")) {
			paths.addAll(paths(condition.get("not")));
		}
		return paths;
	}

}
