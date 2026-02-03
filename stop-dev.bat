@echo off
echo 停止开发服务...

:: 停止 Java 进程 (Spring Boot)
taskkill /F /FI "WINDOWTITLE eq Backend - Spring Boot*" 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do taskkill /F /PID %%a 2>nul

:: 停止 Node 进程 (Vite)
taskkill /F /FI "WINDOWTITLE eq Frontend - Vite*" 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173 ^| findstr LISTENING') do taskkill /F /PID %%a 2>nul

echo 服务已停止
pause
