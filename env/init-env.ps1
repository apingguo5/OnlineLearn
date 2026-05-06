<#
.SYNOPSIS
    OnlineLearn 环境初始化脚本 (PowerShell)
.DESCRIPTION
    从 .env 模板生成本地覆盖文件 .env.local，并检测系统环境
#>

$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "    OnlineLearn 环境初始化脚本" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$envTemplate = Join-Path $scriptDir ".env"
$envLocal    = Join-Path $scriptDir ".env.local"

# 检查是否已有本地覆盖文件
if (Test-Path $envLocal) {
    Write-Host "[提示] 已检测到本地环境配置 (.env.local)" -ForegroundColor Yellow
    Write-Host "       如果要重新初始化，请先删除该文件。" -ForegroundColor Yellow
    Write-Host ""
    $choice = Read-Host "是否重新生成环境配置？(y/N)"
    if ($choice -ne "y" -and $choice -ne "Y") {
        # 跳过生成，直接检测环境
        goto :check_env_ps
    }
}

# 检查 .env 模板
if (-not (Test-Path $envTemplate)) {
    Write-Host "[错误] 未找到模板文件 .env，请确认项目文件完整！" -ForegroundColor Red
    Read-Host "按 Enter 键退出..."
    exit 1
}

# 从模板复制
Copy-Item $envTemplate $envLocal -Force
Write-Host "[成功] 已创建 .env.local（基于 .env 模板）" -ForegroundColor Green
Write-Host ""
Write-Host "请编辑 $envLocal 文件，修改以下配置为您本地路径：" -ForegroundColor Yellow
Write-Host ""
Write-Host "  - JAVA_HOME       JDK 安装目录" -ForegroundColor Yellow
Write-Host "  - MAVEN_HOME      Maven 安装目录" -ForegroundColor Yellow
Write-Host "  - NODE_HOME       Node.js 安装目录" -ForegroundColor Yellow
Write-Host "  - MYSQL_*         MySQL 数据库连接信息" -ForegroundColor Yellow
Write-Host ""
Read-Host "按 Enter 键继续..."

:check_env_ps # PowerShell label for the check section
Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host " 正在检测系统环境..." -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

# 检测 Java
try {
    $javaVer = & java -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        $verLine = $javaVer -join "`n"
        if ($verLine -match '"(\d+\.\d+[^"]*)"') {
            Write-Host "[OK]   Java: $($matches[1])" -ForegroundColor Green
        } else {
            Write-Host "[OK]   Java: 已安装" -ForegroundColor Green
        }
    } else {
        Write-Host "[FAIL] Java: 未找到，请安装 JDK 1.8+" -ForegroundColor Red
    }
} catch {
    Write-Host "[FAIL] Java: 未找到，请安装 JDK 1.8+" -ForegroundColor Red
}

# 检测 Maven
try {
    $mvnVer = & mvn --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        $verLine = $mvnVer -join "`n"
        if ($verLine -match "Apache Maven\s+([\d\.]+)") {
            Write-Host "[OK]   Maven: $($matches[1])" -ForegroundColor Green
        } else {
            Write-Host "[OK]   Maven: 已安装" -ForegroundColor Green
        }
    } else {
        Write-Host "[FAIL] Maven: 未找到，请安装 Maven 3.x" -ForegroundColor Red
    }
} catch {
    Write-Host "[FAIL] Maven: 未找到，请安装 Maven 3.x" -ForegroundColor Red
}

# 检测 Node.js
try {
    $nodeVer = & node --version
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK]   Node: $nodeVer" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] Node: 未找到，请安装 Node.js 12+" -ForegroundColor Red
    }
} catch {
    Write-Host "[FAIL] Node: 未找到，请安装 Node.js 12+" -ForegroundColor Red
}

# 检测 npm
try {
    $npmVer = & npm --version
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK]   npm: v$npmVer" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] npm: 未找到" -ForegroundColor Red
    }
} catch {
    Write-Host "[FAIL] npm: 未找到" -ForegroundColor Red
}

# 检测 MySQL
try {
    $mysqlVer = & mysql --version
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK]   MySQL: 已安装" -ForegroundColor Green
    } else {
        Write-Host "[INFO] MySQL: 未检出客户端（远程数据库可忽略此警告）" -ForegroundColor Yellow
    }
} catch {
    Write-Host "[INFO] MySQL: 未检出客户端（远程数据库可忽略此警告）" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host " 环境初始化完成！" -ForegroundColor Cyan
Write-Host ""
Write-Host " 下一步：" -ForegroundColor Cyan
Write-Host "   1. 编辑 env\.env.local 配置本地路径" -ForegroundColor Cyan
Write-Host "   2. 运行 start.ps1 启动项目" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host ""

Read-Host "按 Enter 键继续..."