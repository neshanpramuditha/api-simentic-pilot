# Runs Schemathesis with the recorded baseline configuration (see README.md in this folder).
# Prerequisites: the API is running on http://localhost:8080 and was started fresh (empty H2 database),
# and Schemathesis is installed from requirements.txt in this folder.
param(
	[Parameter(Mandatory = $true)] [string] $OutDir,
	[string] $Schemathesis = "schemathesis"
)

$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force $OutDir | Out-Null
$env:NO_COLOR = "1"
$env:PYTHONIOENCODING = "utf-8"

# Start every run without Schemathesis's crash cache, matching the conditions of clean-baseline run 1.
if (Test-Path .schemathesis) { Remove-Item -Recurse -Force .schemathesis }

& $Schemathesis run docs/openapi-baseline.json `
	--url http://localhost:8080 `
	--checks all `
	--mode all `
	--phases examples,coverage,fuzzing,stateful `
	--max-examples 50 `
	--seed 42 `
	--workers 1 `
	--generation-database none `
	--no-color `
	--report junit,ndjson,har `
	--report-dir $OutDir 2>&1 | Out-File -Encoding utf8 (Join-Path $OutDir "stdout.txt")

Write-Output "schemathesis exit code: $LASTEXITCODE (1 = at least one failure)"
