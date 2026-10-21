# Stops whatever process is listening on port 8081 (the Library API started by start-library-api.ps1).
$connection = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue
if ($connection) {
	Stop-Process -Id $connection.OwningProcess -Force
	Start-Sleep -Seconds 2
}
$free = -not (Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue)
Write-Output "port 8081 free: $free"
