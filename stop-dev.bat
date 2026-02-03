@echo off
chcp 65001 >nul

echo Stopping dev services...

:: Stop Java process (Spring Boot)
taskkill /F /FI "WINDOWTITLE eq Backend*" 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do taskkill /F /PID %%a 2>nul

:: Stop Node process (Vite)
taskkill /F /FI "WINDOWTITLE eq Frontend*" 2>nul
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173 ^| findstr LISTENING') do taskkill /F /PID %%a 2>nul

echo Services stopped
pause
