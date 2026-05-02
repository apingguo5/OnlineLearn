@echo off
chcp 65001 >nul
title OnlineLearn 启动脚本

rem 设置 Maven 路径
set "MAVEN_HOME=D:\Environment\maven\apache-maven-3.9.14"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

echo ==============================================
echo       OnlineLearn 在线学习平台 - 启动脚本
echo ==============================================
echo.
echo 请选择启动方式：
echo.
echo   [1] 同时启动后端 + 前端
echo   [2] 仅启动后端
echo   [3] 仅启动前端
echo   [4] 安装依赖并同时启动后端 + 前端
echo.
set /p choice="请输入数字 (1-4): "

echo.

if "%choice%"=="1" goto all
if "%choice%"=="2" goto backend
if "%choice%"=="3" goto frontend
if "%choice%"=="4" goto install_all
echo 输入无效，请重新运行脚本。
pause
exit /b

:all
    echo 即将同时启动后端和前端服务...
    echo.
    call :start_backend
    echo 等待 5 秒后启动前端...
    timeout /t 5 /nobreak >nul
    call :start_frontend
    goto end

:backend
    call :start_backend
    goto end

:frontend
    call :start_frontend
    goto end

:install_all
    echo 即将安装依赖并同时启动后端和前端服务...
    echo.
    call :install_backend
    call :install_frontend
    echo.
    call :start_backend
    echo 等待 5 秒后启动前端...
    timeout /t 5 /nobreak >nul
    call :start_frontend
    goto end

:install_backend
    echo [后端] 正在安装 Maven 依赖...
    cd /d "%~dp0OnlineLearnApi - idea"
    call mvn clean install -DskipTests
    if %errorlevel% neq 0 (
        echo [后端] Maven 依赖安装失败！
        pause
        exit /b
    )
    cd /d "%~dp0"
    echo [后端] Maven 依赖安装完成
    exit /b

:install_frontend
    echo [前端] 正在安装 npm 依赖...
    cd /d "%~dp0OnlineLearnVue"
    call npm install --legacy-peer-deps
    if %errorlevel% neq 0 (
        echo [前端] npm 依赖安装失败！
        pause
        exit /b
    )
    cd /d "%~dp0"
    echo [前端] npm 依赖安装完成
    exit /b

:start_backend
    if not exist "%~dp0OnlineLearnApi - idea" (
        echo [后端] 错误: 未找到后端目录 'OnlineLearnApi - idea'
        pause
        exit /b
    )
    echo [后端] 正在启动后端服务...(端口 9251)
    start "OnlineLearn-Backend" cmd /c "cd /d "%~dp0OnlineLearnApi - idea" && mvn spring-boot:run"
    exit /b

:start_frontend
    if not exist "%~dp0OnlineLearnVue" (
        echo [前端] 错误: 未找到前端目录 'OnlineLearnVue'
        pause
        exit /b
    )
    if not exist "%~dp0OnlineLearnVue\node_modules" (
        echo [前端] 提示: node_modules 不存在，将先安装依赖
        cd /d "%~dp0OnlineLearnVue"
        call npm install --legacy-peer-deps
        if %errorlevel% neq 0 (
            echo [前端] npm 依赖安装失败！
            pause
            exit /b
        )
        cd /d "%~dp0"
    )
    
    echo [前端] 正在启动前端服务...(端口 9252)
    
    rem 检测 Node.js 版本
    for /f "tokens=1 delims=v." %%a in ('node --version') do set node_ver=%%a
    if %node_ver% geq 17 (
        start "OnlineLearn-Frontend" cmd /c "cd /d "%~dp0OnlineLearnVue" && set NODE_OPTIONS=--openssl-legacy-provider && npm run serve"
    ) else (
        start "OnlineLearn-Frontend" cmd /c "cd /d "%~dp0OnlineLearnVue" && npm run serve"
    )
    exit /b

:end
echo.
echo ==============================================
echo  后端: http://localhost:9251
echo  前端: http://localhost:9252
echo  请在新打开的窗口中查看服务日志
echo ==============================================
echo.
pause