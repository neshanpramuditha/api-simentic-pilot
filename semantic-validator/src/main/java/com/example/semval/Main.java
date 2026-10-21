package com.example.semval;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import com.example.semval.Finding.Outcome;

/**
 * Command line:
 * <pre>
 * run       --spec openapi.json --rules rules.json --scenario scenario.json --base-url http://localhost:8080 [--jdbc URL] --out report.json
 * check-har --spec openapi.json --rules rules.json --har requests.har.json --out report.json
 * </pre>
 * Exit code 0 = no rule failed, 1 = at least one FAIL, 2 = usage or execution error.
 */
public final class Main {

	public static void main(String[] args) throws Exception {
		if (args.length == 0 || !(args[0].equals("run") || args[0].equals("check-har"))) {
			System.err.println("usage: run|check-har --spec <openapi.json> --rules <rules.json> (--scenario <file> --base-url <url> [--jdbc <url>] | --har <file>) --out <report.json>");
			System.exit(2);
		}
		Map<String, String> options = new HashMap<>();
		for (int i = 1; i + 1 < args.length; i += 2) {
			options.put(args[i].replaceFirst("^--", ""), args[i + 1]);
		}
		OpenApiIndex openApi = new OpenApiIndex(Json.read(Path.of(options.get("spec"))));
		RuleSet rules = new RuleSet(Json.read(Path.of(options.get("rules"))), openApi);
		Engine engine = new Engine(openApi, rules);
		Path out = Path.of(options.get("out"));

		Engine.Result result;
		String source;
		if (args[0].equals("run")) {
			Connection db = options.containsKey("jdbc") ? DriverManager.getConnection(options.get("jdbc"), "sa", "") : null;
			try {
				EvaluationContext context = new EvaluationContext(openApi, options.get("base-url"), db);
				source = "scenario " + Path.of(options.get("scenario")).getFileName() + " against " + options.get("base-url")
						+ (db == null ? " (no database access)" : " (with database access)");
				result = engine.runScenario(Json.read(Path.of(options.get("scenario"))), options.get("base-url"), context);
			} finally {
				if (db != null) {
					db.close();
				}
			}
		} else {
			source = "HAR " + Path.of(options.get("har")).getFileName() + " (passive: no read-backs, no database)";
			result = engine.checkHar(Path.of(options.get("har")), new EvaluationContext(openApi, null, null));
		}

		Map<Outcome, Integer> totals = Report.write(out, args[0], source, rules, result);
		System.out.println("semantic-validator: " + result.exchanges().size() + " exchanges, " + rules.rules().size() + " rules, totals " + totals
				+ ", " + result.durationMillis() + " ms -> " + out);
		result.findings().stream().filter(f -> f.outcome() == Outcome.FAIL)
				.forEach(f -> System.out.println("  FAIL " + f.ruleId() + " #" + f.exchangeIndex() + " " + f.operationId() + ": " + f.message()));
		System.exit(totals.get(Outcome.FAIL) > 0 ? 1 : 0);
	}

}
