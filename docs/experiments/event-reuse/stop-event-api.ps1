# Stops whatever process is listening on port 8082 (the Event API started by start-event-api.ps1).
$connection = Get-NetTCPConnection -LocalPort 8082 -State Listen -ErrorAction SilentlyContinue
if ($connection) {
	Stop-Process -Id $connection.OwningProcess -Force
	Start-Sleep -Seconds 2
}
$free = -not (Get-NetTCPConnection -LocalPort 8082 -State Listen -ErrorAction SilentlyContinue)
Write-Output "port 8082 free: $free"
