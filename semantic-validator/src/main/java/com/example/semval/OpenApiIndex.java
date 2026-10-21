package com.example.semval;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

/** Maps OpenAPI operationIds to method + path template, and matches concrete requests back to operations. */
public final class OpenApiIndex {

	public record Operation(String operationId, String method, String pathTemplate, Pattern pathPattern) {

		public Map<String, String> pathParams(String path) {
			Map<String, String> params = new LinkedHashMap<>();
			Matcher m = pathPattern.matcher(path);
			if (m.matches()) {
				Matcher names = Pattern.compile("\\{([^}]+)}").matcher(pathTemplate);
				int group = 1;
				while (names.find()) {
					params.put(names.group(1), m.group(group++));
				}
			}
			return params;
		}

		public String expand(Map<String, String> params) {
			String path = pathTemplate;
			for (Map.Entry<String, String> e : params.entrySet()) {
				path = path.replace("{" + e.getKey() + "}", e.getValue());
			}
			return path;
		}
	}

	private final Map<String, Operation> byId = new LinkedHashMap<>();

	public OpenApiIndex(JsonNode spec) {
		spec.path("paths").properties().forEach(path -> path.getValue().properties().forEach(op -> {
			String id = op.getValue().path("operationId").asText(null);
			if (id != null) {
				String regex = "^" + path.getKey().replaceAll("\\{[^}]+}", "([^/]+)") + "$";
				byId.put(id, new Operation(id, op.getKey().toUpperCase(), path.getKey(), Pattern.compile(regex)));
			}
		}));
	}

	public Operation require(String operationId) {
		Operation op = byId.get(operationId);
		if (op == null) {
			throw new IllegalArgumentException("operationId not found in OpenAPI document: " + operationId);
		}
		return op;
	}

	public Optional<Operation> match(String method, String path) {
		return byId.values().stream()
				.filter(op -> op.method().equalsIgnoreCase(method) && op.pathPattern().matcher(path).matches())
				.findFirst();
	}

	public int size() {
		return byId.size();
	}

}
