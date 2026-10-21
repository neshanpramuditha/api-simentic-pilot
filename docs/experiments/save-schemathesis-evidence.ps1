# Copies one Schemathesis run directory into docs/experiments with the redactions used for all evidence:
# the absolute local report directory is replaced by <report-dir>\ in stdout.txt (literal) and events.ndjson (JSON-escaped).
# Optional extra files (seeded.json, snapshot-*.json, ...) are copied as-is. Fails if any user path remains.
# Usage: .\docs\experiments\save-schemathesis-evidence.ps1 -RunDir <run> -Destination <docs path> [-ReportRoot <dir>] [-Gzip] [-Extra file1,file2]
param(
	[Parameter(Mandatory = $true)] [string] $RunDir,
	[Parameter(Mandatory = $true)] [string] $Destination,
	[string] $ReportRoot = (Split-Path -Parent (Resolve-Path $RunDir).Path),
	[switch] $Gzip,
	[string[]] $Extra = @()
)

$ErrorActionPreference = "Stop"
$utf8 = New-Object Text.UTF8Encoding($false)
$prefix = (Resolve-Path $ReportRoot).Path.TrimEnd('\') + '\'
New-Item -ItemType Directory -Force $Destination | Out-Null
$dest = (Resolve-Path $Destination).Path

function Write-Output-File([string] $path, [string] $text) {
	if ($Gzip -and ($path -match '\.(ndjson|json)$') -and ($path -match 'events|requests')) {
		$bytes = $utf8.GetBytes($text)
		$out = [IO.File]::Create("$path.gz"); $gz = New-Object IO.Compression.GZipStream($out, [IO.Compression.CompressionLevel]::Optimal)
		$gz.Write($bytes, 0, $bytes.Length); $gz.Dispose(); $out.Dispose()
	} else {
		[IO.File]::WriteAllText($path, $text, $utf8)
	}
}

$stdout = [IO.File]::ReadAllText((Join-Path $RunDir "stdout.txt")).Replace($prefix, "<report-dir>\")
Write-Output-File (Join-Path $dest "stdout.txt") $stdout
Copy-Item (Get-ChildItem $RunDir -Filter "junit-*").FullName (Join-Path $dest "junit.xml")
$ndjson = [IO.File]::ReadAllText((Get-ChildItem $RunDir -Filter "ndjson-*").FullName).Replace($prefix.Replace('\', '\\'), "<report-dir>\\")
Write-Output-File (Join-Path $dest "events.ndjson") $ndjson
Write-Output-File (Join-Path $dest "requests.har.json") ([IO.File]::ReadAllText((Get-ChildItem $RunDir -Filter "har-*").FullName))
foreach ($f in $Extra) { Copy-Item (Join-Path $RunDir $f) (Join-Path $dest $f) }

$userHome = $env:USERPROFILE
foreach ($text in @($stdout, $ndjson)) {
	if ($text.Contains($userHome) -or $text.Contains($userHome.Replace('\', '\\'))) { throw "user path still present after redaction" }
}
Write-Output "saved $RunDir -> $Destination (redacted)"
