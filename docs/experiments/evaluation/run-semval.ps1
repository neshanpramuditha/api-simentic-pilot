# Runs the semantic-validator prototype (semantic-validator/) against the running API, or over a recorded HAR.
# Build first: .\mvnw.cmd -f semantic-validator\pom.xml package; .\mvnw.cmd -f semantic-validator\pom.xml dependency:build-classpath "-Dmdep.outputFile=target/classpath.txt" "-Dmdep.includeScope=runtime"
# Usage:
#   .\docs\experiments\evaluation\run-semval.ps1 -Out <report.json> [-Jdbc <url>]                 # active: shared scenario
#   .\docs\experiments\evaluation\run-semval.ps1 -Out <report.json> -Har <requests.har.json>      # passive: recorded traffic
param(
	[Parameter(Mandatory = $true)] [string] $Out,
	[string] $Jdbc = "",
	[string] $Har = "",
	[string] $Rules = "semantic-validator\rules\order-api.rules.json",
	[string] $Scenario = "docs\experiments\evaluation\scenario.json"
)

$classpath = "semantic-validator\target\semantic-validator-0.1.0-SNAPSHOT.jar;" + (Get-Content "semantic-validator\target\classpath.txt" -Raw).Trim()
$common = @("-cp", $classpath, "com.example.semval.Main")
if ($Har) {
	& java @common check-har --spec docs\openapi-baseline.json --rules $Rules --har $Har --out $Out
} else {
	$runArgs = @("run", "--spec", "docs\openapi-baseline.json", "--rules", $Rules, "--scenario", $Scenario, "--base-url", "http://localhost:8080", "--out", $Out)
	if ($Jdbc) { $runArgs += @("--jdbc", $Jdbc) }
	& java @common @runArgs
}
Write-Output "semantic-validator exit code: $LASTEXITCODE (0 = no rule failed, 1 = at least one FAIL)"
