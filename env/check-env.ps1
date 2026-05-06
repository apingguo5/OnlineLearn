<#
.SYNOPSIS
    OnlineLearn 环境健康检查 (PowerShell)
.DESCRIPTION
    全面检测 JDK / Maven / Node.js / npm / MySQL 环境配置
#>

$ErrorActionPreference = "Continue"
$global:HasError = $false

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "    OnlineLearn 环境健康检查" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 加载环境配置（从 .env / .env.local 读取）
Write-Host "[INFO] 加载环境配置..." -ForegroundColor Gray
. (Join-Path $scriptDir "env-config.ps1")

# 辅助函数
function Write-Result($category, $message, $status) {
    switch ($status) {
        "OK"   { $color = "Green" }
        "FAIL" { $color = "Red"; $global:HasError = $true }
        "WARN" { $color = "Yellow" }
        "INFO" { $color = "Gray" }
    }
    Write-Host "[$status] $category`: $message" -ForegroundColor $color
}

# ----- 1. Java -----
Write-Host ""
Write-Host "----- 1. Java 环境 -----" -ForegroundColor Cyan
if ($script:JAVA_HOME) {
    Write-Result "JAVA_HOME" $script:JAVA_HOME "OK"
    $javaExe = Join-Path $script:JAVA_HOME "bin\java.exe"
    if (Test-Path $javaExe) {
        try {
            $ver = & $javaExe -version 2>&1
            $verStr = ($ver -join "`n")
            if ($verStr -match '"([\d._]+)"') {
                Write-Result "Java" "版本 $($matches[1])" "OK"
            } else {
                Write-Result "Java" "已安装" "OK"
            }
        } catch {
            Write-Result "Java" "版本获取失败" "FAIL"
        }
    } else {
        Write-Result "Java" "可执行文件不存在: $javaExe" "FAIL"
    }
} else {
    try {
        $ver = & java -version 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Result "Java" "系统中找到 Java (未配置 JAVA_HOME)" "OK"
        } else {
            Write-Result "Java" "未找到，请安装 JDK 1.8+" "FAIL"
        }
    } catch {
        Write-Result "Java" "未找到，请安装 JDK 1.8+" "FAIL"
    }
}

# ----- 2. Maven -----
Write-Host ""
Write-Host "----- 2. Maven 环境 -----" -ForegroundColor Cyan
if ($script:MAVEN_HOME) {
    Write-Result "MAVEN_HOME" $script:MAVEN_HOME "OK"
    $mvnExe = Join-Path $script:MAVEN_HOME "bin\mvn.cmd"
    if (Test-Path $mvnExe) {
        try {
            $ver = & $mvnExe --version 2>&1
            $verStr = ($ver -join "`n")
            if ($verStr -match "Apache Maven\s+([\d\.]+)") {
                Write-Result "Maven" "版本 $($matches[1])" "OK"
            } else {
                Write-Result "Maven" "已安装" "OK"
            }
        } catch {
            Write-Result "Maven" "版本获取失败" "FAIL"
        }
    } else {
        Write-Result "Maven" "可执行文件不存在: $mvnExe" "FAIL"
    }
} else {
    try {
        $ver = & mvn --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Result "Maven" "系统中找到 Maven (未配置 MAVEN_HOME)" "OK"
        } else {
            Write-Result "Maven" "未找到，请安装 Maven 3.x" "FAIL"
        }
    } catch {
        Write-Result "Maven" "未找到，请安装 Maven 3.x" "FAIL"
    }
}

# ----- 3. Node.js -----
Write-Host ""
Write-Host "----- 3. Node.js 环境 -----" -ForegroundColor Cyan
if ($script:NODE_HOME) {
    Write-Result "NODE_HOME" $script:NODE_HOME "OK"
    $nodeExe = Join-Path $script:NODE_HOME "node.exe"
    if (Test-Path $nodeExe) {
        try {
            $ver = & $nodeExe --version
            Write-Result "Node.js" $ver "OK"
        } catch {
            Write-Result "Node.js" "版本获取失败" "FAIL"
        }
    } else {
        Write-Result "Node.js" "可执行文件不存在: $nodeExe" "FAIL"
    }
} else {
    try {
        $ver = & node --version
        if ($LASTEXITCODE -eq 0) {
            Write-Result "Node.js" "$ver (未配置 NODE_HOME)" "OK"
        } else {
            Write-Result "Node.js" "未找到，请安装 Node.js 12+" "FAIL"
        }
    } catch {
        Write-Result "Node.js" "未找到，请安装 Node.js 12+" "FAIL"
    }
}

# ----- 4. npm -----
Write-Host ""
Write-Host "----- 4. npm 环境 -----" -ForegroundColor Cyan
try {
    $ver = & npm --version
    if ($LASTEXITCODE -eq 0) {
        Write-Result "npm" "v$ver" "OK"
    } else {
        Write-Result "npm" "未找到" "FAIL"
    }
} catch {
    Write-Result "npm" "未找到" "FAIL"
}

# ----- 5. MySQL -----
Write-Host ""
Write-Host "----- 5. MySQL 数据库 -----" -ForegroundColor Cyan
if ($script:MYSQL_HOST) {
    Write-Result "数据库配置" "$($script:MYSQL_HOST):$($script:MYSQL_PORT)/$($script:MYSQL_DATABASE)" "INFO"
    try {
        $ver = & mysql --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Result "MySQL" "客户端可用" "OK"
        } else {
            Write-Result "MySQL" "客户端未安装（远程数据库可忽略）" "INFO"
        }
    } catch {
        Write-Result "MySQL" "客户端未安装（远程数据库可忽略）" "INFO"
    }
} else {
    Write-Result "MySQL" "未配置连接信息" "INFO"
}

# ----- 6. 项目结构 -----
Write-Host ""
Write-Host "----- 6. 项目结构 -----" -ForegroundColor Cyan
$rootDir = Resolve-Path (Join-Path $scriptDir "..")
$backendPom = Join-Path $rootDir "backend\pom.xml"
$frontendPkg = Join-Path $rootDir "frontend\package.json"

if (Test-Path $backendPom) {
    Write-Result "后端项目" "backend/pom.xml" "OK"
} else {
    Write-Result "后端项目" "未找到 backend/pom.xml" "WARN"
}
if (Test-Path $frontendPkg) {
    Write-Result "前端项目" "frontend/package.json" "OK"
} else {
    Write-Result "前端项目" "未找到 frontend/package.json" "WARN"
}

# 结果汇总
Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
if ($global:HasError) {
    Write-Host " 检查完成：存在 [FAIL] 项，请修复后重试" -ForegroundColor Red
} else {
    Write-Host " 检查完成：所有项通过或仅为警告" -ForegroundColor Green
}
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

Read-Host "按 Enter 键继续..."