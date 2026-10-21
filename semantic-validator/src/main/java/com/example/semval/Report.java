package com.example.semval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.semval.Finding.Outcome;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Writes a JSON report (all exchanges and findings) and a Markdown failure report next to it. */
public final class Report {

	private Report() {
	}

	public static Map<Outcome, Integer> write(Path out, String mode, String source, RuleSet rules, Engine.Result result) throws IOException {
		Map<Outcome, Integer> totals = new LinkedHashMap<>();
		for (Outcome o : Outcome.values()) {
			totals.put(o, 0);
		}
		Map<String, Map<Outcome, Integer>> byRule = new LinkedHashMap<>();
		rules.rules().forEach(r -> {
			Map<Outcome, Integer> m = new LinkedHashMap<>();
			for (Outcome o : Outcome.values()) {
				m.put(o, 0);
			}
			byRule.put(r.id(), m);
		});
		result.findings().forEach(f -> {
			totals.merge(f.outcome(), 1, Integer::sum);
			byRule.get(f.ruleId()).merge(f.outcome(), 1, Integer::sum);
		});

		ObjectNode root = Json.MAPPER.createObjectNode();
		root.put("tool", "semantic-validator 0.2.0");
		root.put("mode", mode);
		root.put("source", source);
		root.put("ruleSet", rules.name());
		root.put("durationMillis", result.durationMillis());
		root.put("exchanges", result.exchanges().size());
		root.put("unmatchedRequests", result.unmatchedRequests());
		root.set("totals", Json.MAPPER.valueToTree(totals));
		root.set("byRule", Json.MAPPER.valueToTree(byRule));
		ArrayNode rulesNode = root.putArray("rules");
		rules.rules().forEach(r -> rulesNode.addObject().put("id", r.id()).put("description", r.description())
				.set("operations", Json.MAPPER.valueToTree(r.operations())));
		ArrayNode failures = root.putArray("failures");
		ArrayNode findings = root.putArray("findings");
		for (Finding f : result.findings()) {
			ObjectNode node = Json.MAPPER.createObjectNode();
			node.put("ruleId", f.ruleId());
			node.put("outcome", f.outcome().name());
			node.put("exchangeIndex", f.exchangeIndex());
			node.put("operationId", f.operationId());
			node.put("message", f.message());
			if (f.evidence() != null) {
				node.set("evidence", f.evidence());
			}
			findings.add(node);
			if (f.outcome() == Outcome.FAIL) {
				failures.add(node);
			}
		}
		Files.createDirectories(out.toAbsolutePath().getParent());
		Files.writeString(out, Json.MAPPER.writeValueAsString(root) + "\n");

		StringBuilder md = new StringBuilder();
		md.append("# Semantic validation report\n\n");
		md.append("- Mode: ").append(mode).append("\n- Source: ").append(source).append("\n- Rule set: ").append(rules.name())
				.append("\n- Exchanges: ").append(result.exchanges().size()).append(", unmatched requests: ").append(result.unmatchedRequests())
				.append("\n- Totals: ").append(totals).append("\n- Duration: ").append(result.durationMillis()).append(" ms\n\n");
		md.append("| Rule | PASS | FAIL | NOT_APPLICABLE |\n|---|---|---|---|\n");
		byRule.forEach((id, m) -> md.append("| ").append(id).append(" | ").append(m.get(Outcome.PASS)).append(" | ")
				.append(m.get(Outcome.FAIL)).append(" | ").append(m.get(Outcome.NOT_APPLICABLE)).append(" |\n"));
		md.append("\n## Failures\n\n");
		if (failures.isEmpty()) {
			md.append("None.\n");
		}
		for (Finding f : result.findings()) {
			if (f.outcome() != Outcome.FAIL) {
				continue;
			}
			md.append("### ").append(f.ruleId()).append(" — exchange #").append(f.exchangeIndex()).append(" (").append(f.operationId()).append(")\n\n");
			md.append(f.message()).append("\n\n```json\n").append(Json.MAPPER.writeValueAsString(f.evidence())).append("\n```\n\n");
		}
		Files.writeString(Path.of(out.toString().replaceAll("\\.json$", "") + ".md"), md.toString());
		return totals;
	}

}
