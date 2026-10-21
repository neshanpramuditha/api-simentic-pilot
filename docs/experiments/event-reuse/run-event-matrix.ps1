# Event Registration API matrix: 6 modes x 4 run types x N repeats. Every run starts the API from scratch on its
# own new file database.
#
# Run types (each a fresh API + database):
#   semval-v01-jdbc   engine 0.1 (original jar, kept aside) + rules v1, active, through record-proxy.js, JDBC
#   semval-v02-jdbc   engine 0.2 (current semantic-validator/ build) + rules v2, active, through record-proxy.js, JDBC
#   capture           capture-har.js sends the scenario; then offline over that one HAR: engine 0.1 + rules v1
#                     passive, engine 0.2 + rules v2 passive, and Ajv schema validation
#   postman           Postman CLI with event.postman_collection.json, through record-proxy.js
# After each run DumpRegistrations.java records the final database while the API is still up.
#
# Usage (project root, after packaging event-api and semantic-validator):
#   .\docs\experiments\event-reuse\run-event-matrix.ps1 -OutRoot <dir, no spaces> -EngineV01 <dir with the 0.1 jar + classpath.txt> -AjvModules <node_modules dir> [-Repeats 2]
param(
	[Parameter(Mandatory = $true)] [string] $OutRoot,
	[Parameter(Mandatory = $true)] [string] $EngineV01,
	[Parameter(Mandatory = $true)] [string] $AjvModules,
	[int] $Repeats = 2
)

$ErrorActionPreference = "Stop"
$here = "docs\experiments\event-reuse"
$spec = "event-api\openapi-baseline.json"
$scenario = "$here\scenario.json"
$rulesV1 = "semantic-validator\rules\event-api.rules.json"
$rulesV2 = "semantic-validator\rules\event-api.rules.v2.json"
$cpV01 = "$EngineV01\semantic-validator-0.1.0-SNAPSHOT.jar;" + (Get-Content "$EngineV01\classpath.txt" -Raw).Trim()
$cpV02 = "semantic-validator\target\semantic-validator-0.1.0-SNAPSHOT.jar;" + (Get-Content "semantic-validator\target\classpath.txt" -Raw).Trim()
$h2 = "$env:USERPROFILE\.m2\repository\com\h2database\h2\2.4.240\h2-2.4.240.jar"
$modes = [ordered]@{
	"clean"     = ""
	"evt-st01"  = "st01-waitlist-confirm-rejected"
	"evt-db01"  = "db01-rejected-change-saved"
	"evt-db02"  = "db02-cancellation-not-saved"
	"evt-ri01"  = "ri01-checkin-without-timestamp"
	"evt-cf01"  = "cf01-checkin-before-confirmation"
}
$runTypes = @("semval-v01-jdbc", "semval-v02-jdbc", "capture", "postman")

function Start-Proxy($runDir) {
	$p = Start-Process node -ArgumentList "docs\experiments\library-reuse\record-proxy.js", "8092", "http://localhost:8082", "$runDir\traffic.har.json" -PassThru -WindowStyle Hidden
	Start-Sleep -Milliseconds 800
	return $p
}

foreach ($mode in $modes.Keys) {
	for ($r = 1; $r -le $Repeats; $r++) {
		foreach ($type in $runTypes) {
			$runDir = Join-Path $OutRoot "$mode\$type-r$r"
			if (Test-Path $runDir) { throw "$runDir already exists; use a new -OutRoot" }
			New-Item -ItemType Directory -Force "$runDir\db" | Out-Null
			$jdbc = "jdbc:h2:file:" + ((Resolve-Path $runDir).Path -replace '\\', '/') + "/db/eventsdb;AUTO_SERVER=TRUE"
			& "$here\start-event-api.ps1" -Defect $modes[$mode] -Log "$runDir\api.log" -DatasourceUrl $jdbc | Out-Null
			# Fail fast if the running jar does not have the requested switch (exactly one DEFECT line, none in clean mode).
			$lines = @(Select-String -Path "$runDir\api.log" -Pattern "DEFECT EVT-")
			if (($modes[$mode] -and $lines.Count -ne 1) -or (-not $modes[$mode] -and $lines.Count -ne 0)) {
				& "$here\stop-event-api.ps1" | Out-Null
				throw "$mode/$type-r${r}: expected $(if ($modes[$mode]) { 1 } else { 0 }) DEFECT line(s), found $($lines.Count); is the event-api jar current?"
			}
			$meta = [ordered]@{ mode = $mode; type = $type; repeat = $r; defect = $modes[$mode] }
			$watch = [System.Diagnostics.Stopwatch]::StartNew()
			switch ($type) {
				{ $_ -like "semval-v0*" } {
					$cp = if ($type -eq "semval-v01-jdbc") { $cpV01 } else { $cpV02 }
					$rules = if ($type -eq "semval-v01-jdbc") { $rulesV1 } else { $rulesV2 }
					$proxy = Start-Proxy $runDir
					$watch.Restart()
					& java -cp $cp com.example.semval.Main run --spec $spec --rules $rules --scenario $scenario --base-url "http://localhost:8092" --jdbc $jdbc --out "$runDir\semval.json" > "$runDir\semval.out.txt"
					$meta.exitCode = $LASTEXITCODE
					$meta.durationMs = $watch.ElapsedMilliseconds
					Stop-Process -Id $proxy.Id -Force
				}
				"capture" {
					& node "$here\..\library-reuse\capture-har.js" $scenario $spec "http://localhost:8082" "$runDir\traffic.har.json" | Out-Null
					$watch.Restart()
					& java -cp $cpV01 com.example.semval.Main check-har --spec $spec --rules $rulesV1 --har "$runDir\traffic.har.json" --out "$runDir\semval-v01-passive.json" > "$runDir\semval-v01-passive.out.txt"
					$meta.exitCodeV01Passive = $LASTEXITCODE
					$meta.durationMsV01Passive = $watch.ElapsedMilliseconds
					$watch.Restart()
					& java -cp $cpV02 com.example.semval.Main check-har --spec $spec --rules $rulesV2 --har "$runDir\traffic.har.json" --out "$runDir\semval-v02-passive.json" > "$runDir\semval-v02-passive.out.txt"
					$meta.exitCodeV02Passive = $LASTEXITCODE
					$meta.durationMsV02Passive = $watch.ElapsedMilliseconds
					$watch.Restart()
					& node "$here\ajv-validate-har.js" $AjvModules $spec "$runDir\traffic.har.json" "$runDir\ajv.json" | Out-Null
					$meta.durationMsAjv = $watch.ElapsedMilliseconds
				}
				"postman" {
					$proxy = Start-Proxy $runDir
					$watch.Restart()
					$ErrorActionPreference = "Continue"
					postman collection run "$here\event.postman_collection.json" --env-var "baseUrl=http://localhost:8092" --no-report-events --color off 2>&1 | Out-File -Encoding utf8 "$runDir\postman.txt"
					$meta.exitCode = $LASTEXITCODE
					$ErrorActionPreference = "Stop"
					$meta.durationMs = $watch.ElapsedMilliseconds
					Stop-Process -Id $proxy.Id -Force
				}
			}
			& java -cp $h2 "$here\DumpRegistrations.java" $jdbc > "$runDir\db-final.json"
			& "$here\stop-event-api.ps1" | Out-Null
			$meta.defectLines = @(Select-String -Path "$runDir\api.log" -Pattern "DEFECT EVT-" | ForEach-Object { ($_.Line -replace '^.*(DEFECT EVT-)', '$1').Trim() })
			$meta | ConvertTo-Json -Depth 3 | Set-Content -Encoding utf8 "$runDir\run.json"
			Write-Output "$mode/$type-r$r done"
		}
	}
}
