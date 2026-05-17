@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

set "ROOT=%~dp0"
if "%ROOT:~-1%"=="\" set "ROOT=%ROOT:~0,-1%"

echo ========================================
echo   物流管理系统 - 停止开发环境
echo ========================================
echo.
echo 正在停止服务并关闭命令行窗口...
echo.

:: 1) 按 start-dev 记录的 cmd 进程 PID 关闭整棵进程树
if exist "%ROOT%\.dev-backend.pid" (
    set /p BACK_PID=<"%ROOT%\.dev-backend.pid"
    if not "!BACK_PID!"=="" taskkill /F /T /PID !BACK_PID! >nul 2>&1
    del /f /q "%ROOT%\.dev-backend.pid" >nul 2>&1
)
if exist "%ROOT%\.dev-frontend.pid" (
    set /p FRONT_PID=<"%ROOT%\.dev-frontend.pid"
    if not "!FRONT_PID!"=="" taskkill /F /T /PID !FRONT_PID! >nul 2>&1
    del /f /q "%ROOT%\.dev-frontend.pid" >nul 2>&1
)

:: 2) 按窗口标题关闭（含旧版 start-dev 与新 title 命令）
for %%T in (
    "LMS-Backend"
    "LMS-Frontend"
    "Backend - Spring Boot"
    "Frontend - Vue"
) do (
    taskkill /FI "WINDOWTITLE eq %%~T" /T /F >nul 2>&1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$titles = @('LMS-Backend','LMS-Frontend','Backend - Spring Boot','Frontend - Vue');" ^
  "Get-Process cmd -ErrorAction SilentlyContinue | Where-Object { $t = $_.MainWindowTitle; $titles | Where-Object { $t -like ('*' + $_ + '*') } } | ForEach-Object { Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue }"

:: 3) 按命令行匹配本项目（兜底）
set "LMS_ROOT=%ROOT%"
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$r = $env:LMS_ROOT;" ^
  "Get-CimInstance Win32_Process -ErrorAction SilentlyContinue | Where-Object {" ^
  "  $c = $_.CommandLine; $c -and (" ^
  "    ($c -match 'LMS-Backend|LMS-Frontend') -or" ^
  "    ($c -like ('*' + $r + '*') -and $c -match 'spring-boot:run|mvnw|pnpm|vite')" ^
  "  )" ^
  "} | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }"

:: 4) 释放端口（结束残留的 java / node）
call :KillPort 8080
call :KillPort 5173

echo.
echo 已停止服务；若仍有空 cmd 窗口，请手动关闭或重新运行本脚本。
echo.
timeout /t 2 /nobreak >nul
exit /b 0

:KillPort
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":%~1 " ^| findstr LISTENING') do (
    if not "%%a"=="0" taskkill /F /PID %%a >nul 2>&1
)
exit /b 0
