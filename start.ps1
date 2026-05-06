<#
.SYNOPSIS
    OnlineLearn 启动脚本 (PowerShell)
.DESCRIPTION
    统一启动脚本，加载 env/ 环境配置后启动后端/前端服务
.PARAMETER All
    同时启动后端和前端 (默认)
.PARAMETER Backend
    仅启动后端
.PARAMETER Frontend
    仅启动前端
.PARAMETER Install
    先安装/更新依赖再启动
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
$EnvDir = Join-Path $RootDir "env"

# ----- 加载环境配置（从 .env / .env.local 读取） -----
$configLoader = Join-Path $EnvDir "env-config.ps1"

if (Test-Path $configLoader) {
    Write-Host "[INFO] Loading config from .env / .env.local" -ForegroundColor Gray
    . $configLoader
} else {
    Write-Host "[WARN] No config file found, using system PATH" -ForegroundColor Yellow
    Write-Host "[WARN] Please run env\init-env.ps1 first" -ForegroundColor Yellow
}

# ----- 从当前作用域读取配置变量（由 dot-source 加载）-----
$script:JAVA_HOME   = if (Test-Path variable:JAVA_HOME)   { $JAVA_HOME }   else { $null }
$script:MAVEN_HOME  = if (Test-Path variable:MAVEN_HOME)  { $MAVEN_HOME }  else { $null }
$script:NODE_HOME   = if (Test-Path variable:NODE_HOME)   { $NODE_HOME }   else { $null }

# 如果没有通过 env-config 定义，尝试读取系统环境变量
if (-not $script:JAVA_HOME -and (Test-Path env:JAVA_HOME))  { $script:JAVA_HOME  = $env:JAVA_HOME }
if (-not $script:MAVEN_HOME -and (Test-Path env:MAVEN_HOME)){ $script:MAVEN_HOME = $env:MAVEN_HOME }
if (-not $script:NODE_HOME -and (Test-Path env:NODE_HOME))  { $script:NODE_HOME  = $env:NODE_HOME }

$script:BACKEND_PORT      = if (Test-Path variable:BACKEND_PORT)      { $BACKEND_PORT }      else { "9251" }
$script:FRONTEND_PORT     = if (Test-Path variable:FRONTEND_PORT)     { $FRONTEND_PORT }     else { "9252" }
$script:SKIP_TESTS        = if (Test-Path variable:SKIP_TESTS)        { $SKIP_TESTS }        else { $true }
$script:NPM_INSTALL_ARGS  = if (Test-Path variable:NPM_INSTALL_ARGS)  { $NPM_INSTALL_ARGS }  else { "--legacy-peer-deps" }

# ----- 默认行为: 同时启动两者 -----
if (-not ($All -or $Backend -or $Frontend)) {
    $All = $true
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "       OnlineLearn Platform - Startup Script" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

function Start-Backend {
    Write-Host "[Backend] Starting backend service..." -ForegroundColor Green
    Write-Host "[Backend] Port: $($script:BACKEND_PORT), Dir: $BackendDir" -ForegroundColor Gray

    if (-not (Test-Path $BackendDir)) {
        Write-Host "[Backend] ERROR: backend directory not found!" -ForegroundColor Red
        return
    }

    if ($Install) {
        Write-Host "[Backend] Installing Maven dependencies..." -ForegroundColor Yellow
        Push-Location $BackendDir
        if ($script:SKIP_TESTS) {
            & mvn clean install -DskipTests
        } else {
            & mvn clean install
        }
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[Backend] Maven install failed!" -ForegroundColor Red
            Pop-Location
            return
        }
        Pop-Location
    }

    # 构建 cmd 命令：在新窗口中设置环境变量后运行 mvn
    $cmdPrologue = ""
    if ($script:JAVA_HOME) {
        $cmdPrologue += "set JAVA_HOME=$($script:JAVA_HOME) && "
        $cmdPrologue += "set PATH=$($script:JAVA_HOME)\bin;%PATH% && "
    }
    if ($script:MAVEN_HOME) {
        $cmdPrologue += "set MAVEN_HOME=$($script:MAVEN_HOME) && "
        $cmdPrologue += "set PATH=$($script:MAVEN_HOME)\bin;%PATH% && "
    }

    $cmdArgs = "/K $cmdPrologue cd /d `"$BackendDir`" && mvn spring-boot:run"
    Start-Process cmd -ArgumentList $cmdArgs

    Write-Host "[Backend] Service starting at http://localhost:$($script:BACKEND_PORT)" -ForegroundColor Green
    Write-Host ""
}

function Start-Frontend {
    Write-Host "[Frontend] Starting frontend service..." -ForegroundColor Green
    Write-Host "[Frontend] Port: $($script:FRONTEND_PORT), Dir: $FrontendDir" -ForegroundColor Gray

    if (-not (Test-Path $FrontendDir)) {
        Write-Host "[Frontend] ERROR: frontend directory not found!" -ForegroundColor Red
        return
    }

    # check node_modules
    $nodeModules = Join-Path $FrontendDir "node_modules"
    if (-not (Test-Path $nodeModules)) {
        Write-Host "[Frontend] node_modules not found, installing..." -ForegroundColor Yellow
        Push-Location $FrontendDir
        & npm install $script:NPM_INSTALL_ARGS
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[Frontend] npm install failed!" -ForegroundColor Red
            Pop-Location
            return
        }
        Pop-Location
    } elseif ($Install) {
        Write-Host "[Frontend] Updating npm dependencies..." -ForegroundColor Yellow
        Push-Location $FrontendDir
        & npm install $script:NPM_INSTALL_ARGS
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[Frontend] npm update failed!" -ForegroundColor Red
            Pop-Location
            return
        }
        Pop-Location
    }

    # 构建 cmd 命令：在新窗口中设置环境变量后运行 npm
    $cmdPrologue = ""
    if ($script:NODE_HOME) {
        $cmdPrologue += "set PATH=$($script:NODE_HOME);%PATH% && "
    }

    # detect node version for OpenSSL compat
    $nodeVer = & node --version
    $nodeMajor = [int]($nodeVer -replace 'v', '' -split '\.')[0]
    $useOpenSSL = $nodeMajor -ge 17

    if ($useOpenSSL) {
        $cmdArgs = "/K $cmdPrologue cd /d `"$FrontendDir`" && set NODE_OPTIONS=--openssl-legacy-provider && npm run serve"
    } else {
        $cmdArgs = "/K $cmdPrologue cd /d `"$FrontendDir`" && npm run serve"
    }
    Start-Process cmd -ArgumentList $cmdArgs

    Write-Host "[Frontend] Service starting at http://localhost:$($script:FRONTEND_PORT)" -ForegroundColor Green
    Write-Host ""
}

# ----- 主执行逻辑 -----
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
    Write-Host " Backend: http://localhost:$($script:BACKEND_PORT)" -ForegroundColor Cyan
    Write-Host " Frontend: http://localhost:$($script:FRONTEND_PORT)" -ForegroundColor Cyan
    Write-Host " Services are running in separate windows." -ForegroundColor Cyan
    Write-Host "==============================================" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Hint: Services are running in cmd windows." -ForegroundColor Gray