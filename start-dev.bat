@echo off
chcp 65001 >nul

echo ========================================
echo   物流管理系统 - 开发环境启动
echo ========================================
echo.

:: Check MySQL and create database
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

:: Start Backend (new window)
echo [2/3] 启动后端服务...
start "Backend - Spring Boot" cmd /k "chcp 65001 >nul && cd /d %~dp0javaService\demo && echo 正在启动后端服务... && mvnw spring-boot:run"

:: Wait for backend
echo      等待后端启动...
timeout /t 5 /nobreak >nul

:: Start Frontend (new window)
echo [3/3] 启动前端服务...
start "Frontend - Vue" cmd /k "chcp 65001 >nul && cd /d %~dp0frontEnd && echo 正在启动前端服务... && pnpm dev"

echo.
echo ========================================
echo   启动完成！
echo   后端: http://localhost:8080
echo   前端: http://localhost:5173
echo ========================================
echo.

:: Close this window after 3 seconds
timeout /t 3 /nobreak >nul
exit
