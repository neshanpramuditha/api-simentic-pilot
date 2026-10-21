# Library Lending API reuse matrix: 4 modes x 3 validator configurations x N repeats. Every run starts the API from
# scratch on its own new file database, so no run sees another run's state.
#
# Configurations:
#   semval-jdbc     active: scenario through record-proxy.js (ground-truth traffic), JDBC access to the run's database
#   semval-nojdbc   active: same, without a JDBC URL (database evidence unavailable)
#   semval-passive  passive: capture-har.js sends the scenario, then check-har over the recorded HAR
# After the validator, DumpLoans.java records the final database state while the API is still running.
#
# Usage (from the project root, after packaging library-api and semantic-validator, see README.md):
#   .\docs\experiments\library-reuse\run-library-matrix.ps1 -OutRoot <dir without spaces> [-Repeats 2]
param(
	[Parameter(Mandatory = $true)] [string] $OutRoot,
	[int] $Repeats = 2
)

$ErrorActionPreference = "Stop"
$here = "docs\experiments\library-reuse"
$spec = "library-api\openapi-baseline.json"
$rules = "semantic-validator\rules\library-api.rules.json"
$scenario = "$here\scenario.json"
$classpath = "semantic-validator\target\semantic-validator-0.1.0-SNAPSHOT.jar;" + (Get-Content "semantic-validator\target\classpath.txt" -Raw).Trim()
$h2 = "$env:USERPROFILE\.m2\repository\com\h2database\h2\2.4.240\h2-2.4.240.jar"
$modes = [ordered]@{
	"clean"    = ""
	"lib-st01" = "st01-requested-to-issued"
	"lib-db01" = "db01-rejected-change-saved"
	"lib-ri01" = "ri01-returned-without-timestamp"
}
$configs = @("semval-jdbc", "semval-nojdbc", "semval-passive")

foreach ($mode in $modes.Keys) {
	foreach ($config in $configs) {
		for ($r = 1; $r -le $Repeats; $r++) {
			$runDir = Join-Path $OutRoot "$mode\$config-r$r"
			if (Test-Path $runDir) { throw "$runDir already exists; use a new -OutRoot" }
			New-Item -ItemType Directory -Force "$runDir\db" | Out-Null
			$jdbc = "jdbc:h2:file:" + ((Resolve-Path $runDir).Path -replace '\\', '/') + "/db/librarydb;AUTO_SERVER=TRUE"
			& "$here\start-library-api.ps1" -Defect $modes[$mode] -Log "$runDir\api.log" -DatasourceUrl $jdbc | Out-Null

			$watch = [System.Diagnostics.Stopwatch]::StartNew()
			if ($config -eq "semval-passive") {
				& node "$here\capture-har.js" $scenario $spec "http://localhost:8081" "$runDir\traffic.har.json" | Out-Null
				$watch.Restart()
				& java -cp $classpath com.example.semval.Main check-har --spec $spec --rules $rules --har "$runDir\traffic.har.json" --out "$runDir\semval.json" > "$runDir\semval.out.txt"
				$exit = $LASTEXITCODE
			} else {
				$proxy = Start-Process node -ArgumentList "$here\record-proxy.js", "8091", "http://localhost:8081", "$runDir\traffic.har.json" -PassThru -WindowStyle Hidden
				Start-Sleep -Milliseconds 800
				$runArgs = @("run", "--spec", $spec, "--rules", $rules, "--scenario", $scenario, "--base-url", "http://localhost:8091", "--out", "$runDir\semval.json")
				if ($config -eq "semval-jdbc") { $runArgs += @("--jdbc", $jdbc) }
				$watch.Restart()
				& java -cp $classpath com.example.semval.Main @runArgs > "$runDir\semval.out.txt"
				$exit = $LASTEXITCODE
				Stop-Process -Id $proxy.Id -Force
			}
			$duration = $watch.ElapsedMilliseconds
			& java -cp $h2 "$here\DumpLoans.java" $jdbc > "$runDir\db-final.json"
			& "$here\stop-library-api.ps1" | Out-Null

			$defectLines = @(Select-String -Path "$runDir\api.log" -Pattern "DEFECT LIB-" | ForEach-Object { ($_.Line -replace '^.*(DEFECT LIB-)', '$1').Trim() })
			[ordered]@{ mode = $mode; config = $config; repeat = $r; defect = $modes[$mode]; exitCode = $exit; durationMs = $duration; defectLines = $defectLines } |
				ConvertTo-Json -Depth 3 | Set-Content -Encoding utf8 "$runDir\run.json"
			& node "$here\verify-run.js" $runDir $scenario $spec
		}
	}
}
