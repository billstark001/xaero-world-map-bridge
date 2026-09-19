param(
    [int]$TimeoutSeconds = 180
)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$gradleWrapper = Join-Path $projectRoot "gradlew.bat"
$targets = @("1.21.11", "26.1.2", "26.2", "26.3")
$smokeLogDirectory = Join-Path $projectRoot "build/neoforge-smoke-logs"
New-Item -ItemType Directory -Path $smokeLogDirectory -Force | Out-Null

function Stop-ProcessTree {
    param([int]$ProcessId)

    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId = $ProcessId" -ErrorAction SilentlyContinue)
    foreach ($child in $children) {
        Stop-ProcessTree -ProcessId $child.ProcessId
    }
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}

foreach ($target in $targets) {
    $startedAt = Get-Date
    $log = Join-Path $projectRoot "run/logs/latest.log"
    $arguments = @(
        ":versions:neoforge-${target}:runClient",
        "--console=plain",
        "--no-daemon",
        "--project-cache-dir",
        (Join-Path $projectRoot "build/neoforge-smoke-cache/$target")
    )
    $runId = Get-Date -Format "yyyyMMdd-HHmmss-fff"
    $standardOutput = Join-Path $smokeLogDirectory "$target-$runId.out.log"
    $standardError = Join-Path $smokeLogDirectory "$target-$runId.err.log"
    Write-Host "Starting NeoForge $target smoke client..."
    $process = Start-Process `
        -FilePath $gradleWrapper `
        -ArgumentList $arguments `
        -WorkingDirectory $projectRoot `
        -WindowStyle Hidden `
        -RedirectStandardOutput $standardOutput `
        -RedirectStandardError $standardError `
        -PassThru
    $passed = $false
    $failure = $null

    try {
        $deadline = $startedAt.AddSeconds($TimeoutSeconds)
        while ((Get-Date) -lt $deadline) {
            if ($process.HasExited) {
                $failure = (
                    "Gradle exited before the client reached the title screen " +
                    "(exit $($process.ExitCode)); inspect $standardOutput and $standardError."
                )
                break
            }

            if (Test-Path -LiteralPath $log) {
                $logItem = Get-Item -LiteralPath $log
                if ($logItem.LastWriteTime -ge $startedAt) {
                    $content = Get-Content -LiteralPath $log -Raw
                    if ($content -match "InjectionError|InvalidInjection|MixinApplyError|MixinTransformerError") {
                        $failure = "A Mixin error was reported in $log."
                        break
                    }
                    $bridgeLoaded = $content -match "xaero_world_map_bridge"
                    $resourcesLoaded = $content -match "mod/xaero_world_map_bridge|Reloading ResourceManager"
                    $clientReady = $content -match "Sound engine started|SoundEngine.*started"
                    if ($bridgeLoaded -and $resourcesLoaded -and $clientReady) {
                        $passed = $true
                        break
                    }
                }
            }
            Start-Sleep -Seconds 2
            $process.Refresh()
        }
    } finally {
        if (-not $process.HasExited) {
            Stop-ProcessTree -ProcessId $process.Id
            $process.WaitForExit(10000) | Out-Null
        }
    }

    if (-not $passed) {
        if ($null -eq $failure) {
            $failure = "Timed out after $TimeoutSeconds seconds; inspect $log."
        }
        throw "NeoForge $target smoke test failed: $failure"
    }
    Write-Host "NeoForge $target smoke test passed."
}

Write-Host "All NeoForge startup smoke tests passed."
