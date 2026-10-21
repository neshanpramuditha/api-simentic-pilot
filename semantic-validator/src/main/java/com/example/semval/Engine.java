package com.example.semval;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/** Executes a scenario against a live API (active mode) or replays recorded traffic from a HAR file (passive mode). */
public final class Engine {

	public record Result(List<Exchange> exchanges, List<Finding> findings, int unmatchedRequests, long durationMillis) {
	}

	private final OpenApiIndex openApi;
	private final RuleSet rules;

	public Engine(OpenApiIndex openApi, RuleSet rules) {
		this.openApi = openApi;
		this.rules = rules;
	}

	public Result runScenario(JsonNode scenario, String baseUrl, EvaluationContext context) throws IOException, InterruptedException {
		long start = System.nanoTime();
		HttpClient http = HttpClient.newHttpClient();
		Map<String, String> variables = new HashMap<>();
		List<Exchange> exchanges = new ArrayList<>();
		List<Finding> findings = new ArrayList<>();
		for (JsonNode step : scenario.get("steps")) {
			OpenApiIndex.Operation op = openApi.require(step.get("operationId").asText());
			Map<String, String> pathParams = new LinkedHashMap<>();
			step.path("pathParams").properties().forEach(e -> pathParams.put(e.getKey(), substitute(e.getValue().asText(), variables)));
			JsonNode body = step.has("body") ? step.get("body") : Json.MAPPER.nullNode();

			List<Rule> applicable = rules.forOperation(op.operationId());
			Map<Rule, JsonNode> before = new HashMap<>();
			for (Rule rule : applicable) {
				before.put(rule, rule.before(context, op.operationId(), pathParams, body));
			}

			String path = op.expand(pathParams);
			HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + path)).header("Accept", "application/json");
			if (body.isNull()) {
				request.method(op.method(), HttpRequest.BodyPublishers.noBody());
			} else {
				request.header("Content-Type", "application/json").method(op.method(), HttpRequest.BodyPublishers.ofString(body.toString()));
			}
			long sent = System.nanoTime();
			HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
			Exchange exchange = new Exchange(exchanges.size(), op.operationId(), op.method(), path, pathParams, body,
					response.statusCode(), Json.parseOrNull(response.body()), (System.nanoTime() - sent) / 1_000_000);
			exchanges.add(exchange);

			for (Rule rule : applicable) {
				findings.addAll(rule.evaluate(exchange, before.get(rule), context));
			}
			for (Rule rule : rules.rules()) {
				rule.observe(exchange);
			}
			step.path("save").properties().forEach(e -> variables.put(e.getKey(), Json.text(exchange.resolve(e.getValue().asText()))));
		}
		return new Result(exchanges, findings, 0, (System.nanoTime() - start) / 1_000_000);
	}

	public Result checkHar(Path harFile, EvaluationContext context) throws IOException {
		long start = System.nanoTime();
		List<Exchange> exchanges = new ArrayList<>();
		List<Finding> findings = new ArrayList<>();
		int unmatched = 0;
		for (JsonNode entry : Json.read(harFile).path("log").path("entries")) {
			URI uri = URI.create(entry.path("request").path("url").asText());
			String method = entry.path("request").path("method").asText();
			var op = openApi.match(method, uri.getRawPath());
			if (op.isEmpty()) {
				unmatched++;
				continue;
			}
			JsonNode requestBody = Json.parseOrNull(entry.path("request").path("postData").path("text").asText(null));
			Exchange exchange = new Exchange(exchanges.size(), op.get().operationId(), method, uri.getRawPath(),
					op.get().pathParams(uri.getRawPath()), requestBody, entry.path("response").path("status").asInt(),
					Json.parseOrNull(entry.path("response").path("content").path("text").asText(null)),
					entry.path("time").asLong());
			exchanges.add(exchange);
			for (Rule rule : rules.forOperation(op.get().operationId())) {
				findings.addAll(rule.evaluate(exchange, null, context));
			}
			for (Rule rule : rules.rules()) {
				rule.observe(exchange);
			}
		}
		return new Result(exchanges, findings, unmatched, (System.nanoTime() - start) / 1_000_000);
	}

	private static String substitute(String template, Map<String, String> variables) {
		String result = template;
		for (Map.Entry<String, String> e : variables.entrySet()) {
			result = result.replace("${" + e.getKey() + "}", e.getValue());
		}
		if (result.contains("${")) {
			throw new IllegalArgumentException("unresolved variable in " + template);
		}
		return result;
	}

}
