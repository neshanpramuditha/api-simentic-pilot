# Starts the API fresh (empty H2 database) in clean mode or with one defect enabled, and waits until it is ready.
# Usage:  .\docs\experiments\start-api.ps1 [-Defect sv01-total-as-string] [-Log <file>]
#         .\docs\experiments\stop-api.ps1
param(
	[string] $Defect = "",
	[string] $Log = "$env:TEMP\api-semantic-pilot.log"
)

$ErrorActionPreference = "Stop"
if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
	throw "Port 8080 is already in use; run stop-api.ps1 first."
}

$arguments = @("-B", "-q", "spring-boot:run")
if ($Defect) {
	$arguments += "-Dspring-boot.run.arguments=--pilot.defects.$Defect.enabled=true"
}
Start-Process -FilePath ".\mvnw.cmd" -ArgumentList $arguments -WindowStyle Hidden -RedirectStandardOutput $Log | Out-Null

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
