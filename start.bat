@echo off
chcp 65001 >nul
title OnlineLearn 启动脚本

rem ==============================================
rem  OnlineLearn 在线学习平台 - 统一启动脚本
rem ==============================================
rem
rem  使用方式：
rem    start.bat              - 菜单式选择启动方式
rem    start.bat all           - 同时启动后端+前端
rem    start.bat backend       - 仅启动后端
rem    start.bat frontend      - 仅启动前端
rem    start.bat install       - 安装依赖并同时启动
rem
rem ==============================================

set "ROOT_DIR=%~dp0"

rem ----- 加载环境配置（从 .env / .env.local 读取） -----
if exist "%ROOT_DIR%env\env-config.bat" (
    call "%ROOT_DIR%env\env-config.bat"
) else (
    echo [WARN] 未找到环境配置文件，尝试使用系统默认 PATH
    echo [WARN] 请先运行 env\init-env.bat 初始化环境配置
    echo.
)

rem ----- 设置 JDK 路径 -----
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

rem ----- 设置 Maven 路径 -----
if defined MAVEN_HOME (
    set "PATH=%MAVEN_HOME%\bin;%PATH%"
)

rem ----- 设置 Node.js 路径 -----
if defined NODE_HOME (
    set "PATH=%NODE_HOME%;%PATH%"
)

rem ----- 设置默认端口（如果环境配置未定义） -----
if not defined BACKEND_PORT set "BACKEND_PORT=9251"
if not defined FRONTEND_PORT set "FRONTEND_PORT=9252"

rem ----- 设置默认选项 -----
if not defined SKIP_TESTS set "SKIP_TESTS=true"
if not defined NPM_INSTALL_ARGS set "NPM_INSTALL_ARGS=--legacy-peer-deps"

rem ----- 解析命令行参数 -----
set "MODE="
if /i "%1"=="all"       set "MODE=all"
if /i "%1"=="backend"   set "MODE=backend"
if /i "%1"=="frontend"  set "MODE=frontend"
if /i "%1"=="install"   set "MODE=install"

if defined MODE goto %MODE%

rem ---- 菜单模式 ----
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

:install
    echo 即将安装依赖...
    echo.
    call :install_backend
    call :install_frontend
    echo 依赖安装完成！
    goto end

rem ----- 后端操作 -----
:install_backend
    if not exist "%ROOT_DIR%backend" (
        echo [后端] 错误: 未找到后端目录
        exit /b 1
    )
    echo [后端] 正在安装 Maven 依赖...
    cd /d "%ROOT_DIR%backend"
    if "%SKIP_TESTS%"=="true" (
        call mvn clean install -DskipTests
    ) else (
        call mvn clean install
    )
    if %errorlevel% neq 0 (
        echo [后端] Maven 依赖安装失败！
        pause
        exit /b
    )
    cd /d "%ROOT_DIR%"
    echo [后端] Maven 依赖安装完成
    exit /b

:start_backend
    if not exist "%ROOT_DIR%backend" (
        echo [后端] 错误: 未找到后端目录
        pause
        exit /b
    )
    echo [后端] 正在启动后端服务...(端口 %BACKEND_PORT%)

    rem 新开的 cmd 窗口不继承当前进程的 PATH 修改，需在新窗口中重新 set
    set "CMD_SETUP="
    if defined JAVA_HOME (
        set "CMD_SETUP=set JAVA_HOME=%JAVA_HOME% & set PATH=%JAVA_HOME%\bin;%%PATH%% & "
    )
    if defined MAVEN_HOME (
        set "CMD_SETUP=%CMD_SETUP%set MAVEN_HOME=%MAVEN_HOME% & set PATH=%MAVEN_HOME%\bin;%%PATH%% & "
    )
    start "OnlineLearn-Backend" cmd /c "%CMD_SETUP% cd /d "%ROOT_DIR%backend" && mvn spring-boot:run"
    exit /b

rem ----- 前端操作 -----
:install_frontend
    if not exist "%ROOT_DIR%frontend" (
        echo [前端] 错误: 未找到前端目录
        exit /b 1
    )
    echo [前端] 正在安装 npm 依赖...
    cd /d "%ROOT_DIR%frontend"
    call npm install %NPM_INSTALL_ARGS%
    if %errorlevel% neq 0 (
        echo [前端] npm 依赖安装失败！
        pause
        exit /b
    )
    cd /d "%ROOT_DIR%"
    echo [前端] npm 依赖安装完成
    exit /b

:start_frontend
    if not exist "%ROOT_DIR%frontend" (
        echo [前端] 错误: 未找到前端目录
        pause
        exit /b
    )
    if not exist "%ROOT_DIR%frontend\node_modules" (
        echo [前端] 提示: node_modules 不存在，将先安装依赖
        cd /d "%ROOT_DIR%frontend"
        call npm install %NPM_INSTALL_ARGS%
        if %errorlevel% neq 0 (
            echo [前端] npm 依赖安装失败！
            pause
            exit /b
        )
        cd /d "%ROOT_DIR%"
    )
    
    echo [前端] 正在启动前端服务...(端口 %FRONTEND_PORT%)

    rem 新开的 cmd 窗口不继承当前进程的 PATH 修改
    set "CMD_SETUP="
    if defined NODE_HOME (
        set "CMD_SETUP=set PATH=%NODE_HOME%;%%PATH%% & "
    )
    
    rem 检测 Node.js 版本，17+ 需要 --openssl-legacy-provider
    for /f "tokens=1 delims=v." %%a in ('node --version') do set node_ver=%%a
    if %node_ver% geq 17 (
        start "OnlineLearn-Frontend" cmd /c "%CMD_SETUP% cd /d "%ROOT_DIR%frontend" && set NODE_OPTIONS=--openssl-legacy-provider && npm run serve"
    ) else (
        start "OnlineLearn-Frontend" cmd /c "%CMD_SETUP% cd /d "%ROOT_DIR%frontend" && npm run serve"
    )
    exit /b

:end
echo.
echo ==============================================
echo  后端: http://localhost:%BACKEND_PORT%
echo  前端: http://localhost:%FRONTEND_PORT%
echo  请在新打开的窗口中查看服务日志
echo ==============================================
echo.
pause