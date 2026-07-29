param(
    [Parameter(Mandatory = $true)]
    [string]$Jar,
    [string]$OutputDirectory = "build/xaero-inspection"
)

$ErrorActionPreference = "Stop"
$resolvedJar = (Resolve-Path -LiteralPath $Jar).Path
$resolvedOutput = [System.IO.Path]::GetFullPath($OutputDirectory)
New-Item -ItemType Directory -Force -Path $resolvedOutput | Out-Null

$javap = Get-Command javap -ErrorAction Stop
$outputFile = Join-Path $resolvedOutput "GuiMap.javap.txt"

& $javap.Source -classpath $resolvedJar -c -p xaero.map.gui.GuiMap |
    Tee-Object -FilePath $outputFile

Write-Host "Saved Xaero GuiMap disassembly to $outputFile"
