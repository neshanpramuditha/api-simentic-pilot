# Runs the comparative evaluation: every tool x every mode x repeats, each tool run on a freshly started API with its
# own empty file-based H2 database (AUTO_SERVER, so the semantic validator can read it). One defect switch at most.
# Usage: .\docs\experiments\evaluation\run-matrix.ps1 -OutRoot <dir> -Schemathesis <schemathesis.exe> -AjvModules <node_modules> [-Repeats 2]
param(
	[Parameter(Mandatory = $true)] [string] $OutRoot,
	[Parameter(Mandatory = $true)] [string] $Schemathesis,
	[Parameter(Mandatory = $true)] [string] $AjvModules,
	[int] $Repeats = 2,
	[string[]] $Modes = @("clean", "sv01-total-as-string", "st01-completed-to-cancelled", "db02-rejected-change-saved"),
	[string[]] $Tools = @("ajv", "postman", "semval", "schemathesis-baseline", "schemathesis-seeded")
)

$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force $OutRoot | Out-Null
$OutRoot = (Resolve-Path $OutRoot).Path
$env:NODE_PATH = $AjvModules
$summary = @()

function Start-Fresh([string] $mode, [string] $dir) {
	$db = Join-Path $dir "db"; New-Item -ItemType Directory -Force $db | Out-Null
	$jdbc = "jdbc:h2:file:$($db.Replace('\','/'))/semanticdb;AUTO_SERVER=TRUE"
	$defect = if ($mode -eq "clean") { "" } else { $mode }
	if ($defect) { .\docs\experiments\start-api.ps1 -Defect $defect -Log (Join-Path $dir "api.log") -DatasourceUrl $jdbc | Out-Null }
	else { .\docs\experiments\start-api.ps1 -Log (Join-Path $dir "api.log") -DatasourceUrl $jdbc | Out-Null }
	$warnings = @(Select-String -Path (Join-Path $dir "api.log") -Pattern 'DEFECT [A-Z]{2}-\d+ ENABLED' -AllMatches | ForEach-Object { $_.Matches.Value })
	return @{ jdbc = $jdbc; warnings = ($warnings -join ",") }
}

foreach ($mode in $Modes) {
	foreach ($r in 1..$Repeats) {
		foreach ($tool in $Tools) {
			$dir = Join-Path $OutRoot "$mode\$tool-r$r"; New-Item -ItemType Directory -Force $dir | Out-Null
			$api = Start-Fresh $mode $dir
			$t0 = Get-Date
			switch ($tool) {
				"ajv" {
					node docs\experiments\evaluation\capture-scenario.js docs\experiments\evaluation\scenario.json docs\openapi-baseline.json (Join-Path $dir "responses.json") | Out-Null
					node docs\experiments\schema-only\validate.js docs\openapi-baseline.json (Join-Path $dir "responses.json") | Out-File -Encoding utf8 (Join-Path $dir "result.txt")
				}
				"postman" {
					$env:NO_COLOR = "1"
					postman collection run docs\experiments\evaluation\evaluation.postman_collection.json --no-report-events --color off 2>&1 | Out-File -Encoding utf8 (Join-Path $dir "result.txt")
				}
				"semval" {
					.\docs\experiments\evaluation\run-semval.ps1 -Out (Join-Path $dir "report.json") -Jdbc $api.jdbc | Out-File -Encoding utf8 (Join-Path $dir "result.txt")
				}
				"schemathesis-baseline" {
					.\docs\experiments\schemathesis\run-schemathesis.ps1 -OutDir $dir -Schemathesis $Schemathesis | Out-Null
				}
				"schemathesis-seeded" {
					node docs\experiments\seed-orders.js 30 COMPLETED (Join-Path $dir "seeded.json") | Out-Null
					$t0 = Get-Date
					.\docs\experiments\schemathesis\run-schemathesis.ps1 -OutDir $dir -Schemathesis $Schemathesis | Out-Null
				}
			}
			$seconds = [math]::Round(((Get-Date) - $t0).TotalSeconds, 1)
			if ($tool -eq "schemathesis-seeded") { node docs\experiments\snapshot-orders.js (Join-Path $dir "seeded.json") (Join-Path $dir "snapshot-after.json") | Out-Null }
			.\docs\experiments\stop-api.ps1 | Out-Null
			$summary += [pscustomobject]@{ mode = $mode; tool = $tool; repeat = $r; seconds = $seconds; warnings = $api.warnings; dir = $dir.Substring($OutRoot.Length + 1) }
			Write-Output ("{0,-30} {1,-22} r{2} {3,6}s warnings=[{4}]" -f $mode, $tool, $r, $seconds, $api.warnings)
		}
	}
}
$summary | ConvertTo-Json | Out-File -Encoding utf8 (Join-Path $OutRoot "runs.json")
Write-Output "done: $($summary.Count) runs -> $OutRoot"
