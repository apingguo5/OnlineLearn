<#
.SYNOPSIS
    OnlineLearn 环境配置加载器 (PowerShell)
.DESCRIPTION
    从 .env 或 .env.local 读取统一配置，设置为当前作用域的变量
#>

$script:EnvDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 优先加载 .env.local（本地覆盖），否则加载 .env（默认模板）
$envFile = Join-Path $script:EnvDir ".env.local"
if (-not (Test-Path $envFile)) {
    $envFile = Join-Path $script:EnvDir ".env"
}
if (-not (Test-Path $envFile)) {
    Write-Warning "[WARN] 未找到环境文件 .env 或 .env.local"
    Write-Warning "[WARN] 请先运行 env\init-env.ps1 初始化"
    return
}

# 读取 .env 文件，逐行解析 KEY=VALUE，跳过注释和空行
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and $line -notmatch '^\s*#') {
        $parts = $line -split '=', 2
        if ($parts.Count -eq 2) {
            $key = $parts[0].Trim()
            $value = $parts[1].Trim()
            Set-Variable -Name $key -Value $value -Scope Script
        }
    }
}