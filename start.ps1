<#
.SYNOPSIS
    OnlineLearn startup script (PowerShell).
.DESCRIPTION
    Start frontend (Vue), backend (Spring Boot), or both.
.PARAMETER All
    Start both frontend and backend (default).
.PARAMETER Backend
    Start backend only.
.PARAMETER Frontend
    Start frontend only.
.PARAMETER Install
    Install/update dependencies before starting.
.EXAMPLE
    .\start.ps1 -All
    .\start.ps1 -Backend
    .\start.ps1 -Frontend
    .\start.ps1 -All -Install
#>

param(
    [switch]$All,
    [switch]$Backend,
    [switch]$Frontend,
    [switch]$Install
)

$RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackendDir = Join-Path $RootDir "backend"
$FrontendDir = Join-Path $RootDir "frontend"

# Maven 路径（已安装但需显式设置供新窗口使用）
$MAVEN_HOME = "D:\Environment\maven\apache-maven-3.9.14"
$MAVEN_BIN = "$MAVEN_HOME\bin"
$env:PATH = "$MAVEN_BIN;$env:PATH"

# default: start both
if (-not ($All -or $Backend -or $Frontend)) {
    $All = $true
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "       OnlineLearn Platform - Startup Script    " -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

function Start-Backend {
    Write-Host "[Backend] Starting backend service..." -ForegroundColor Green
    Write-Host "[Backend] Port: 9251, Dir: $BackendDir" -ForegroundColor Gray

    if (-not (Test-Path $BackendDir)) {
        Write-Host "[Backend] ERROR: backend directory not found!" -ForegroundColor Red
        return
    }

    if ($Install) {
        Write-Host "[Backend] Installing Maven dependencies..." -ForegroundColor Yellow
        Push-Location $BackendDir
        mvn clean install -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[Backend] Maven install failed!" -ForegroundColor Red
            Pop-Location
            return
        }
        Pop-Location
    }

    # open new cmd window to run backend
    $cmdArg = 'cd /d "' + $BackendDir + '" && mvn spring-boot:run'
    Start-Process cmd -ArgumentList @('/K', $cmdArg) -WindowStyle Normal
    Write-Host "[Backend] Service starting at http://localhost:9251" -ForegroundColor Green
    Write-Host ""
}

function Start-Frontend {
    Write-Host "[Frontend] Starting frontend service..." -ForegroundColor Green
    Write-Host "[Frontend] Port: 9252, Dir: $FrontendDir" -ForegroundColor Gray

    if (-not (Test-Path $FrontendDir)) {
        Write-Host "[Frontend] ERROR: frontend directory not found!" -ForegroundColor Red
        return
    }

    # check node_modules
    $nodeModules = Join-Path $FrontendDir "node_modules"
    if (-not (Test-Path $nodeModules)) {
        Write-Host "[Frontend] node_modules not found, installing..." -ForegroundColor Yellow
        Push-Location $FrontendDir
        npm install --legacy-peer-deps
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[Frontend] npm install failed!" -ForegroundColor Red
            Pop-Location
            return
        }
        Pop-Location
    } elseif ($Install) {
        Write-Host "[Frontend] Updating npm dependencies..." -ForegroundColor Yellow
        Push-Location $FrontendDir
        npm install --legacy-peer-deps
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[Frontend] npm update failed!" -ForegroundColor Red
            Pop-Location
            return
        }
        Pop-Location
    }

    # detect node version
    $nodeVersion = [int]((node --version) -replace 'v', '' -split '\.')[0]
    $useOpenSSL = $nodeVersion -ge 17

    # open new cmd window to run frontend
    $dirQuoted = '"' + $FrontendDir + '"'
    if ($useOpenSSL) {
        $cmdArg = "cd /d $dirQuoted && set NODE_OPTIONS=--openssl-legacy-provider && npm run serve"
    } else {
        $cmdArg = "cd /d $dirQuoted && npm run serve"
    }
    Start-Process cmd -ArgumentList @('/K', $cmdArg) -WindowStyle Normal

    Write-Host "[Frontend] Service starting at http://localhost:9252" -ForegroundColor Green
    Write-Host ""
}

# main execution
if ($Backend) {
    Start-Backend
} elseif ($Frontend) {
    Start-Frontend
} elseif ($All) {
    Write-Host "Starting backend and frontend together..." -ForegroundColor Cyan
    Write-Host ""
    Start-Backend
    Write-Host "Waiting 5 seconds before starting frontend..." -ForegroundColor Yellow
    Start-Sleep -Seconds 5
    Start-Frontend
    Write-Host "==============================================" -ForegroundColor Cyan
    Write-Host " Backend: http://localhost:9251" -ForegroundColor Cyan
    Write-Host " Frontend: http://localhost:9252" -ForegroundColor Cyan
    Write-Host " Both services are running in separate windows." -ForegroundColor Cyan
    Write-Host "==============================================" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Hint: Services are running in separate windows." -ForegroundColor Gray