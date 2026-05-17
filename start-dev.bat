@echo off
chcp 65001 >nul
setlocal

set "ROOT=%~dp0"
if "%ROOT:~-1%"=="\" set "ROOT=%ROOT:~0,-1%"

echo ========================================
echo   物流管理系统 - 开发环境启动
echo ========================================
echo.

echo [1/3] 检查MySQL数据库...
mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS logistics DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
if %errorlevel% neq 0 (
    echo [警告] MySQL连接失败，请确保MySQL服务已启动
    echo        数据库: localhost:3306
    echo        用户名: root
    echo        密码: 123456
    echo.
    pause
)

echo [2/3] 启动后端服务...
powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT%\scripts\start-dev-window.ps1" -Kind backend -Root "%ROOT%"
if %errorlevel% neq 0 (
    echo [错误] 后端启动失败
    pause
    exit /b 1
)

echo      等待后端启动...
timeout /t 5 /nobreak >nul

echo [3/3] 启动前端服务...
powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT%\scripts\start-dev-window.ps1" -Kind frontend -Root "%ROOT%"
if %errorlevel% neq 0 (
    echo [错误] 前端启动失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo   启动完成！
echo   后端: http://localhost:8080
echo   前端: http://localhost:5173
echo ========================================
echo.

timeout /t 3 /nobreak >nul
exit /b 0
