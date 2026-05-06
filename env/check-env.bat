@echo off
chcp 65001 >nul
title OnlineLearn 环境检查

echo ==============================================
echo    OnlineLearn 环境健康检查
echo ==============================================
echo.

setlocal enabledelayedexpansion
set "HAS_ERROR=0"
set "ENV_DIR=%~dp0"

rem --- 加载环境配置（从 .env / .env.local 读取） ---
echo [INFO] 加载环境配置...
call "%ENV_DIR%env-config.bat"

rem --- 1. 检查 JDK/JAVA ---
echo.
echo ----- 1. Java 环境 -----
if defined JAVA_HOME (
    echo [OK]   JAVA_HOME = %JAVA_HOME%
    if exist "%JAVA_HOME%\bin\java.exe" (
        for /f "tokens=3" %%i in ('"%JAVA_HOME%\bin\java" -version 2^>^&1 ^| findstr /i "version"') do (
            echo [OK]   Java %%i
        )
    ) else (
        echo [FAIL] %JAVA_HOME%\bin\java.exe 不存在
        set HAS_ERROR=1
    )
) else (
    where java >nul 2>&1
    if !errorlevel! equ 0 (
        echo [OK]   系统中已找到 Java (未配置 JAVA_HOME)
        java -version
    ) else (
        echo [FAIL] 未找到 Java，请安装 JDK 1.8+
        set HAS_ERROR=1
    )
)

rem --- 2. 检查 Maven ---
echo.
echo ----- 2. Maven 环境 -----
if defined MAVEN_HOME (
    echo [OK]   MAVEN_HOME = %MAVEN_HOME%
    if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        for /f "tokens=3" %%i in ('"%MAVEN_HOME%\bin\mvn" --version 2^>^&1 ^| findstr /i "Apache Maven"') do (
            echo [OK]   Maven %%i
        )
    ) else (
        echo [FAIL] %MAVEN_HOME%\bin\mvn.cmd 不存在
        set HAS_ERROR=1
    )
) else (
    where mvn >nul 2>&1
    if !errorlevel! equ 0 (
        echo [OK]   系统中已找到 Maven (未配置 MAVEN_HOME)
    ) else (
        echo [FAIL] 未找到 Maven，请安装 Maven 3.x
        set HAS_ERROR=1
    )
)

rem --- 3. 检查 Node.js ---
echo.
echo ----- 3. Node.js 环境 -----
if defined NODE_HOME (
    echo [OK]   NODE_HOME = %NODE_HOME%
    if exist "%NODE_HOME%\node.exe" (
        "%NODE_HOME%\node" --version
        echo [OK]   Node.js 可执行文件正常
    ) else (
        echo [FAIL] %NODE_HOME%\node.exe 不存在
        set HAS_ERROR=1
    )
) else (
    where node >nul 2>&1
    if !errorlevel! equ 0 (
        for /f "tokens=1 delims=v" %%i in ('node --version') do set NODE_VER=%%i
        echo [OK]   Node.js v!NODE_VER! (未配置 NODE_HOME)
    ) else (
        echo [FAIL] 未找到 Node.js，请安装 Node.js 12+
        set HAS_ERROR=1
    )
)

rem --- 4. 检查 npm ---
echo.
echo ----- 4. npm 环境 -----
where npm >nul 2>&1
if !errorlevel! equ 0 (
    for /f "delims=" %%i in ('npm --version') do set NPM_VER=%%i
    echo [OK]   npm v!NPM_VER!
) else (
    echo [FAIL] 未找到 npm
    set HAS_ERROR=1
)

rem --- 5. 检查 MySQL 可连接性（可选） ---
echo.
echo ----- 5. MySQL 数据库 -----
if defined MYSQL_HOST (
    echo [INFO] 数据库配置: %MYSQL_HOST%:%MYSQL_PORT%/%MYSQL_DATABASE%
    where mysql >nul 2>&1
    if !errorlevel! equ 0 (
        echo [INFO] MySQL 客户端可用
    ) else (
        echo [INFO] MySQL 客户端未安装（远程数据库可忽略）
    )
) else (
    echo [INFO] 未配置 MySQL 连接信息
)

rem --- 6. 检查项目结构 ---
echo.
echo ----- 6. 项目结构 -----
if exist "%ENV_DIR%..\backend\pom.xml" (
    echo [OK]   后端项目: backend/pom.xml
) else (
    echo [WARN] 未找到 backend/pom.xml
)
if exist "%ENV_DIR%..\frontend\package.json" (
    echo [OK]   前端项目: frontend/package.json
) else (
    echo [WARN] 未找到 frontend/package.json
)

echo.
echo ==============================================
if "!HAS_ERROR!"=="1" (
    echo  检查完成：存在 [FAIL] 项，请修复后重试
) else (
    echo  检查完成：所有项通过或仅为警告
)
echo ==============================================
echo.
endlocal
pause