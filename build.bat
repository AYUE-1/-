@echo off
chcp 65001 >nul
echo ============================================
echo   宠物领养平台 - 一键打包脚本
echo ============================================
echo.

REM 设置路径
set "PROJECT_DIR=%~dp0"
set "FRONTEND_DIR=%PROJECT_DIR%pet-adoption-frontend"
set "BACKEND_DIR=%PROJECT_DIR%pet-adoption-backend"
set "STATIC_DIR=%BACKEND_DIR%\src\main\resources\static"

REM 步骤 1: 构建前端
echo [1/3] 构建前端...
cd /d "%FRONTEND_DIR%"
call npm run build
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 前端构建失败！
    pause
    exit /b 1
)
echo ✅ 前端构建完成

REM 步骤 2: 复制前端文件到后端 static 目录
echo.
echo [2/3] 复制前端文件到后端...
if exist "%STATIC_DIR%" rmdir /s /q "%STATIC_DIR%"
mkdir "%STATIC_DIR%"
xcopy /e /y "%FRONTEND_DIR%\dist\*" "%STATIC_DIR%\"
echo ✅ 复制完成

REM 步骤 3: 构建后端 JAR
echo.
echo [3/3] 构建后端 JAR...
cd /d "%BACKEND_DIR%"
set "MAVEN_HOME=%BACKEND_DIR%\apache-maven-3.9.6"
set "JAVA_HOME=C:\Program Files\Java\jdk-17.0.2"
"%MAVEN_HOME%\bin\mvn.cmd" clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 后端构建失败！
    pause
    exit /b 1
)
echo ✅ 后端构建完成

REM 显示结果
echo.
echo ============================================
echo   ✅ 打包完成！
echo ============================================
echo.
echo   输出文件: target\pet-adoption-backend-1.0.0.jar
echo   运行方式: java -jar target\pet-adoption-backend-1.0.0.jar
echo.
echo   然后访问: http://localhost:8080
echo.
pause
