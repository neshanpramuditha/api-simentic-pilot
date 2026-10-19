# Stops whatever process is listening on port 8080 (the API started by start-api.ps1).
$connection = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($connection) {
	Stop-Process -Id $connection.OwningProcess -Force
	Start-Sleep -Seconds 2
}
$free = -not (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue)
Write-Output "port 8080 free: $free"
