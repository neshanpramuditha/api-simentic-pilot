# Starts the Event Registration API (event-api/, port 8082) from its packaged jar, in clean mode or with one defect
# enabled, and waits until it is ready. Package first: .\mvnw.cmd -f event-api\pom.xml package -DskipTests
# Usage:  .\docs\experiments\event-reuse\start-event-api.ps1 [-Defect <switch name, see defects.md>] [-Log <file>] [-DatasourceUrl <jdbc url>]
#         .\docs\experiments\event-reuse\stop-event-api.ps1
# -DatasourceUrl overrides spring.datasource.url for this process only (via SPRING_DATASOURCE_URL), e.g. a file-based
# H2 database with AUTO_SERVER=TRUE so the semantic validator can read the same database.
param(
	[string] $Defect = "",
	[string] $Log = "$env:TEMP\event-api.log",
	[string] $DatasourceUrl = "",
	[string] $Jar = "event-api\target\event-api-0.0.1-SNAPSHOT.jar"
)

$ErrorActionPreference = "Stop"
if (Get-NetTCPConnection -LocalPort 8082 -State Listen -ErrorAction SilentlyContinue) {
	throw "Port 8082 is already in use; run stop-event-api.ps1 first."
}
if (-not (Test-Path $Jar)) {
	throw "$Jar not found; package event-api first."
}

$arguments = @("-jar", $Jar)
if ($Defect) {
	$arguments += "--events.defects.$Defect.enabled=true"
}
$previousUrl = $env:SPRING_DATASOURCE_URL
if ($DatasourceUrl) { $env:SPRING_DATASOURCE_URL = $DatasourceUrl }
try {
	Start-Process -FilePath "java" -ArgumentList $arguments -WindowStyle Hidden -RedirectStandardOutput $Log | Out-Null
} finally {
	$env:SPRING_DATASOURCE_URL = $previousUrl
}

for ($i = 0; $i -lt 90; $i++) {
	try {
		Invoke-WebRequest "http://localhost:8082/v3/api-docs" -UseBasicParsing -TimeoutSec 2 | Out-Null
		$mode = if ($Defect) { "defect mode ($Defect)" } else { "clean mode" }
		Write-Output "Event API ready on http://localhost:8082 in $mode"
		return
	} catch {
		Start-Sleep -Seconds 1
	}
}
throw "Event API did not start within 90 s; see $Log"
