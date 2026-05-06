@echo off
rem ==============================================
rem  OnlineLearn 环境配置加载器 (CMD)
rem  从 .env 或 .env.local 读取统一配置
rem ==============================================
rem
rem  设计要点：
rem    1. 不使用 setlocal/endlocal（避免变量传递问题）
rem    2. 直接在当前作用域 set 变量
rem    3. 优先 .env.local（本地覆盖），兜底 .env（默认模板）
rem
rem ==============================================

set "ENV_DIR=%~dp0"

rem ----- 优先加载 .env.local，否则加载 .env -----
if exist "%ENV_DIR%.env.local" (
    set "ENV_FILE=%ENV_DIR%.env.local"
) else if exist "%ENV_DIR%.env" (
    set "ENV_FILE=%ENV_DIR%.env"
) else (
    echo [WARN] 未找到环境文件 .env 或 .env.local
    echo [WARN] 请先运行 env\init-env.bat 初始化
    goto :eof
)

rem ----- 逐行读取 .env，解析 KEY=VALUE -----
rem   findstr 过滤掉 # 开头的注释行和空行
for /f "tokens=1,* delims==" %%a in ('
    type "%ENV_FILE%" ^| findstr /v /b "#" ^| findstr /r /c:"^[A-Za-z_]"
') do (
    set "%%a=%%b"
)