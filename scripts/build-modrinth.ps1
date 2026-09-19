[CmdletBinding()]
param(
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$distributionDirectory = Join-Path $repositoryRoot "build\modrinth"
$versionsDirectory = Join-Path $repositoryRoot "versions"

if (-not $SkipBuild) {
    & (Join-Path $repositoryRoot "gradlew.bat") buildAll
    if ($LASTEXITCODE -ne 0) {
        throw "The complete build failed with exit code $LASTEXITCODE."
    }
}

if (Test-Path -LiteralPath $distributionDirectory) {
    Remove-Item -LiteralPath $distributionDirectory -Recurse -Force
}
New-Item -ItemType Directory -Path $distributionDirectory -Force | Out-Null

$artifacts = Get-ChildItem -Path $versionsDirectory -Recurse -File -Filter "xaero-world-map-bridge-*.jar" |
    Where-Object { $_.Name -notmatch "-(sources|dev|javadoc)\.jar$" } |
    Sort-Object Name

if ($artifacts.Count -ne 8) {
    throw "Expected eight distributable JARs after building, but found $($artifacts.Count)."
}

foreach ($artifact in $artifacts) {
    Copy-Item -LiteralPath $artifact.FullName -Destination $distributionDirectory
}

Write-Host "Collected $($artifacts.Count) Modrinth upload files in $distributionDirectory"
$artifacts | ForEach-Object { Write-Host " - $($_.Name)" }
