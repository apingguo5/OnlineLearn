@echo off
chcp 65001 >nul
title OnlineLearn 环境初始化

echo ==============================================
echo    OnlineLearn 环境初始化脚本
echo ==============================================
echo.

set "ENV_DIR=%~dp0"

rem 检查是否已存在本地环境覆盖文件
if exist "%ENV_DIR%.env.local" (
    echo [提示] 已检测到本地环境配置 (.env.local)
    echo       如果要重新初始化，请先删除该文件。
    echo.
    choice /c YN /m "是否重新生成环境配置？"
    if errorlevel 2 goto check_env
)

rem 检查模板文件是否存在
if not exist "%ENV_DIR%.env" (
    echo [错误] 未找到模板文件 .env，请确认项目文件完整！
    pause
    exit /b 1
)

echo 正在从模板复制环境配置文件...
copy /Y "%ENV_DIR%.env" "%ENV_DIR%.env.local" >nul
if %errorlevel% neq 0 (
    echo [错误] 复制环境配置文件失败！
    pause
    exit /b 1
)
echo [成功] 已创建 .env.local（基于 .env 模板）
echo.
echo 请编辑 %ENV_DIR%.env.local 文件，修改以下配置为您本地路径：
echo.
echo   - JAVA_HOME       JDK 安装目录
echo   - MAVEN_HOME      Maven 安装目录
echo   - NODE_HOME       Node.js 安装目录
echo   - MYSQL_*         MySQL 数据库连接信息
echo.
pause

:check_env
echo.
echo ==============================================
echo  正在检测系统环境...
echo ==============================================
echo.

rem --- 检测 Java ---
where java >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VER=%%i
    )
    echo [OK]   Java: %JAVA_VER%
) else (
    echo [FAIL] Java: 未找到，请安装 JDK 1.8+
)

rem --- 检测 Maven ---
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=3" %%i in ('mvn --version 2^>^&1 ^| findstr /i "Apache Maven"') do (
        set MAVEN_VER=%%i
    )
    echo [OK]   Maven: %MAVEN_VER%
) else (
    echo [FAIL] Maven: 未找到，请安装 Maven 3.x
)

rem --- 检测 Node.js ---
where node >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=1 delims=v" %%i in ('node --version') do set NODE_VER=%%i
    echo [OK]   Node: v%NODE_VER%
) else (
    echo [FAIL] Node: 未找到，请安装 Node.js 12+
)

rem --- 检测 npm ---
where npm >nul 2>&1
if %errorlevel% equ 0 (
    for /f "delims=" %%i in ('npm --version') do set NPM_VER=%%i
    echo [OK]   npm: v%NPM_VER%
) else (
    echo [FAIL] npm: 未找到
)

rem --- 检测 MySQL ---
where mysql >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK]   MySQL: 已安装
) else (
    echo [INFO] MySQL: 未检出客户端（远程数据库可忽略此警告）
)

echo.
echo ==============================================
echo  环境初始化完成！
echo.
echo  下一步：
echo    1. 编辑 env\.env.local 配置本地路径
echo    2. 运行 start.bat 启动项目
echo ==============================================
echo.
pause