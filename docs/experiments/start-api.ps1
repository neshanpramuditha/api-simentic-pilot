# Starts the API fresh (empty H2 database) in clean mode or with one defect enabled, and waits until it is ready.
# Usage:  .\docs\experiments\start-api.ps1 [-Defect sv01-total-as-string] [-Log <file>] [-DatasourceUrl <jdbc url>]
#         .\docs\experiments\stop-api.ps1
# -DatasourceUrl overrides spring.datasource.url for this process only (via SPRING_DATASOURCE_URL), e.g. a file-based
# H2 database with AUTO_SERVER=TRUE so a second process (the semantic validator) can read the same database.
param(
	[string] $Defect = "",
	[string] $Log = "$env:TEMP\api-semantic-pilot.log",
	[string] $DatasourceUrl = ""
)

$ErrorActionPreference = "Stop"
if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
	throw "Port 8080 is already in use; run stop-api.ps1 first."
}

$arguments = @("-B", "-q", "spring-boot:run")
if ($Defect) {
	$arguments += "-Dspring-boot.run.arguments=--pilot.defects.$Defect.enabled=true"
}
$previousUrl = $env:SPRING_DATASOURCE_URL
if ($DatasourceUrl) { $env:SPRING_DATASOURCE_URL = $DatasourceUrl }
try {
	Start-Process -FilePath ".\mvnw.cmd" -ArgumentList $arguments -WindowStyle Hidden -RedirectStandardOutput $Log | Out-Null
} finally {
	$env:SPRING_DATASOURCE_URL = $previousUrl
}

for ($i = 0; $i -lt 120; $i++) {
	try {
		Invoke-WebRequest "http://localhost:8080/v3/api-docs" -UseBasicParsing -TimeoutSec 2 | Out-Null
		$mode = if ($Defect) { "defect mode ($Defect)" } else { "clean mode" }
		Write-Output "API ready on http://localhost:8080 in $mode; log: $Log"
		return
	} catch {
		Start-Sleep -Seconds 1
	}
}
throw "API did not start within 120 s; see $Log"
