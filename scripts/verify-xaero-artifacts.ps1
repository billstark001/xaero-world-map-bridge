param(
    [ValidateSet("all", "fabric", "neoforge")]
    [string]$Loader = "all",
    [string]$OutputDirectory = "build/xaero-inspection"
)

$ErrorActionPreference = "Stop"
$headers = @{ "User-Agent" = "xaero-world-map-bridge-ci/0.1.0" }
$outputRoot = [System.IO.Path]::GetFullPath($OutputDirectory)
$downloadRoot = Join-Path $outputRoot "artifacts"
New-Item -ItemType Directory -Force -Path $downloadRoot | Out-Null

$targets = @(
    @{ Minecraft = "1.21.11"; Minors = @(40, 41, 42, 43, 44); MojmapAnchor = "MultiBufferSource`$BufferSource"; FabricRawAnchor = "class_4597`$class_4598" },
    @{ Minecraft = "26.1.2"; Minors = @(40, 41, 42, 43, 44); MojmapAnchor = "MultiBufferSource`$BufferSource"; FabricRawAnchor = "MultiBufferSource`$BufferSource" },
    @{ Minecraft = "26.2"; Minors = @(41, 42, 43, 44); MojmapAnchor = "XaeroBufferProvider"; FabricRawAnchor = "XaeroBufferProvider" }
)

$loaders = if ($Loader -eq "all") { @("fabric", "neoforge") } else { @($Loader) }
$versions = Invoke-RestMethod -Headers $headers -Uri "https://api.modrinth.com/v2/project/NcUtCpym/version"
$javap = (Get-Command javap -ErrorAction Stop).Source
$failures = New-Object System.Collections.Generic.List[string]

foreach ($target in $targets) {
    foreach ($targetLoader in $loaders) {
        $published = @($versions | Where-Object {
            $_.loaders -contains $targetLoader -and $_.game_versions -contains $target.Minecraft
        })

        foreach ($minor in $target.Minors) {
            $minorPattern = "1\.$minor(?:[._]|$)"
            $matching = @($published | Where-Object { $_.version_number -match $minorPattern })
            if ($matching.Count -eq 0) {
                $failures.Add("Missing $targetLoader artifact for Minecraft $($target.Minecraft), Xaero 1.$minor")
                continue
            }

            foreach ($version in $matching) {
                $file = $version.files | Where-Object { $_.primary } | Select-Object -First 1
                if ($null -eq $file) {
                    $failures.Add("No primary file for $($version.version_number)")
                    continue
                }

                $safeName = "$targetLoader-$($target.Minecraft)-$($version.id).jar".Replace("/", "_")
                $jar = Join-Path $downloadRoot $safeName
                if (-not (Test-Path -LiteralPath $jar)) {
                    Invoke-WebRequest -Headers $headers -Uri $file.url -OutFile $jar
                }

                $disassembly = & $javap -classpath $jar -c -p xaero.map.gui.GuiMap 2>&1 | Out-String
                $anchor = if ($targetLoader -eq "fabric") { $target.FabricRawAnchor } else { $target.MojmapAnchor }
                if ($disassembly -notmatch [regex]::Escape("MapElementRenderHandler.render") -or
                    $disassembly -notmatch [regex]::Escape($anchor)) {
                    $failures.Add("Exact anchor changed in $($version.version_number) ($targetLoader / $($target.Minecraft))")
                    continue
                }
                Write-Host "Verified $($version.version_number) ($targetLoader / $($target.Minecraft))"
            }
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host "All published Xaero 1.40--1.44 targets match the recorded exact anchors."
