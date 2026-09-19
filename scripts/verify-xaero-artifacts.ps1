param(
    [ValidateSet("all", "fabric", "neoforge")]
    [string]$Loader = "all",
    [string]$OutputDirectory = "build/xaero-inspection"
)

$ErrorActionPreference = "Stop"
$headers = @{ "User-Agent" = "xaero-world-map-bridge-ci/0.1.1" }
$outputRoot = [System.IO.Path]::GetFullPath($OutputDirectory)
$downloadRoot = Join-Path $outputRoot "artifacts"
New-Item -ItemType Directory -Force -Path $downloadRoot | Out-Null

$targets = @(
    @{
        Minecraft = "1.21.11"
        Minors = @(40, 41, 42, 43, 44, 45, 46)
        FabricRenderMethod = "method_25394"
        NeoForgeRenderMethod = "render"
        FabricDescriptor = 'xaero/map/element/MapElementRenderHandler.render:(Lxaero/map/gui/GuiMap;Lnet/minecraft/class_4597$class_4598;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/class_310;F)Lxaero/map/element/HoveredMapElementHolder;'
        NeoForgeDescriptor = 'xaero/map/element/MapElementRenderHandler.render:(Lxaero/map/gui/GuiMap;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;'
    },
    @{
        Minecraft = "26.1.2"
        Minors = @(40, 41, 42, 43, 44, 45, 46)
        FabricRenderMethod = "extractRenderState"
        NeoForgeRenderMethod = "extractRenderState"
        FabricDescriptor = 'xaero/map/element/MapElementRenderHandler.render:(Lxaero/map/gui/GuiMap;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;'
        NeoForgeDescriptor = 'xaero/map/element/MapElementRenderHandler.render:(Lxaero/map/gui/GuiMap;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;'
    },
    @{
        Minecraft = "26.2"
        Minors = @(41, 42, 43, 44, 45, 46)
        FabricRenderMethod = "extractRenderState"
        NeoForgeRenderMethod = "extractRenderState"
        FabricDescriptor = 'xaero/map/element/MapElementRenderHandler.render:(Lxaero/map/gui/GuiMap;Lxaero/lib/client/graphics/XaeroBufferProvider;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;'
        NeoForgeDescriptor = 'xaero/map/element/MapElementRenderHandler.render:(Lxaero/map/gui/GuiMap;Lxaero/lib/client/graphics/XaeroBufferProvider;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;'
    },
    @{
        Minecraft = "26.3"
        Minors = @(46)
        FabricRenderMethod = "extractRenderState"
        NeoForgeRenderMethod = "extractRenderState"
        FabricDescriptor = 'xaero/map/element/MapElementRenderHandler.render:(Lxaero/map/gui/GuiMap;Lxaero/lib/client/graphics/XaeroBufferProvider;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;'
        NeoForgeDescriptor = 'xaero/map/element/MapElementRenderHandler.render:(Lxaero/map/gui/GuiMap;Lxaero/lib/client/graphics/XaeroBufferProvider;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;'
    }
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

                $disassemblyLines = @(& $javap -classpath $jar -c -p xaero.map.gui.GuiMap 2>&1)
                $renderMethod = if ($targetLoader -eq "fabric") {
                    $target.FabricRenderMethod
                } else {
                    $target.NeoForgeRenderMethod
                }
                $methodStart = -1
                for ($lineIndex = 0; $lineIndex -lt $disassemblyLines.Count; $lineIndex++) {
                    if ($disassemblyLines[$lineIndex] -match "^\s*public void $([regex]::Escape($renderMethod))\(") {
                        $methodStart = $lineIndex
                        break
                    }
                }
                if ($methodStart -lt 0) {
                    $failures.Add("Missing $renderMethod in $($version.version_number) ($targetLoader / $($target.Minecraft))")
                    continue
                }

                $methodEnd = $disassemblyLines.Count
                for ($lineIndex = $methodStart + 1; $lineIndex -lt $disassemblyLines.Count; $lineIndex++) {
                    if ($disassemblyLines[$lineIndex] -match "^\s{2}(public|protected|private) .+;\s*$") {
                        $methodEnd = $lineIndex
                        break
                    }
                }
                $methodBody = ($disassemblyLines[$methodStart..($methodEnd - 1)] -join " ") -replace "\s+", " "
                $descriptor = if ($targetLoader -eq "fabric") {
                    $target.FabricDescriptor
                } else {
                    $target.NeoForgeDescriptor
                }
                $anchorCount = [regex]::Matches($methodBody, [regex]::Escape($descriptor)).Count
                if ($anchorCount -ne 1) {
                    $failures.Add("Expected exactly one exact anchor, found $anchorCount in $($version.version_number) ($targetLoader / $($target.Minecraft))")
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

Write-Host "All published Xaero 1.40--1.46 targets match the recorded exact anchors."
