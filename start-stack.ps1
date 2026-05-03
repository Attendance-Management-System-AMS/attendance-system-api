$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

if (-not (Test-Path -LiteralPath (Join-Path $root "mvnw.cmd"))) {
    throw "Cannot find mvnw.cmd in $root"
}

function Import-EnvFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        return
    }

    Write-Host "Loading environment from $(Split-Path -Leaf $Path)" -ForegroundColor Cyan
    Get-Content -LiteralPath $Path | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) {
            return
        }

        $separatorIndex = $line.IndexOf("=")
        if ($separatorIndex -le 0) {
            return
        }

        $key = $line.Substring(0, $separatorIndex).Trim()
        $value = $line.Substring($separatorIndex + 1).Trim()
        [Environment]::SetEnvironmentVariable($key, $value, "Process")
    }
}

$envFiles = @(
    Join-Path $root ".env",
    Join-Path $root ".env.development",
    Join-Path $root ".env.local",
    Join-Path $root ".env.development.local"
)

foreach ($envFile in $envFiles) {
    Import-EnvFile -Path $envFile
}

$defaults = @{
    EUREKA_SERVER_PORT = "8761"
    API_GATEWAY_PORT = "9000"
    HR_SERVICE_PORT = "9001"
    ATTENDANCE_SERVICE_PORT = "9002"
    AUTH_SERVICE_PORT = "9004"
}

foreach ($entry in $defaults.GetEnumerator()) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($entry.Key, "Process"))) {
        [Environment]::SetEnvironmentVariable($entry.Key, $entry.Value, "Process")
    }
}

$modules = @(
    @{ Name = "eureka-server"; Port = [int]$env:EUREKA_SERVER_PORT; DelaySeconds = 8 },
    @{ Name = "hr-service"; Port = [int]$env:HR_SERVICE_PORT; DelaySeconds = 3 },
    @{ Name = "attendance-service"; Port = [int]$env:ATTENDANCE_SERVICE_PORT; DelaySeconds = 2 },
    @{ Name = "auth-service"; Port = [int]$env:AUTH_SERVICE_PORT; DelaySeconds = 2 },
    @{ Name = "api-gateway"; Port = [int]$env:API_GATEWAY_PORT; DelaySeconds = 0 }
)

Write-Host "Starting microservice modules. Run '.\mvnw.cmd -pl monolith-service spring-boot:run' separately if you need the fallback monolith." -ForegroundColor Yellow

foreach ($module in $modules) {
    $name = $module.Name
    $port = $module.Port

    $listener = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($listener) {
        $process = Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue
        $processName = if ($process) { $process.ProcessName } else { "unknown" }
        Write-Host "Skipping $name because port $port is already used by PID $($listener.OwningProcess) ($processName)." -ForegroundColor Yellow
        continue
    }

    Write-Host "Starting $name on port $port..." -ForegroundColor Cyan
    Start-Process powershell.exe -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location -LiteralPath '$root'; .\mvnw.cmd -pl $name spring-boot:run"
    )

    if ($module.DelaySeconds -gt 0) {
        Start-Sleep -Seconds $module.DelaySeconds
    }
}
