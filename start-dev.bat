@echo off
chcp 65001 >nul

:: Start Backend (new window)
start "Backend" cmd /k "chcp 65001 >nul && cd /d %~dp0javaService\demo && mvnw spring-boot:run -Dspring-boot.run.profiles=dev"

:: Wait for backend
timeout /t 3 /nobreak >nul

:: Start Frontend (new window)
start "Frontend" cmd /k "chcp 65001 >nul && cd /d %~dp0frontEnd && pnpm dev"

:: Close this window immediately
exit
