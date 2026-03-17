@echo off
REM Android自动化部署脚本
REM 用法: auto_deploy.bat [选项]

setlocal enabledelayedexpansion

echo ========================================
echo Android自动化构建部署脚本
echo ========================================

REM 检查Python
python --version >nul 2>&1
if errorlevel 1 (
    echo 错误: Python未安装或不在PATH中
    echo 请安装Python 3.6+并添加到系统PATH
    pause
    exit /b 1
)

REM 检查adb
if not exist "platform-tools\platform-tools\adb.exe" (
    echo 警告: adb未找到
    echo 正在下载platform-tools...
    
    powershell -Command "Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/platform-tools-latest-windows.zip' -OutFile 'platform-tools.zip' -UseBasicParsing"
    if errorlevel 1 (
        echo 下载失败
        pause
        exit /b 1
    )
    
    powershell -Command "Expand-Archive -Path 'platform-tools.zip' -DestinationPath '.' -Force"
    move platform-tools\platform-tools platform-tools\platform-tools2 >nul 2>&1
    rmdir /s /q platform-tools\platform-tools2 >nul 2>&1
    echo 平台工具下载完成
)

REM 检查GitHub Token
if "%GITHUB_TOKEN%"=="" (
    echo 提示: 设置GITHUB_TOKEN环境变量以提高API限额
    echo setx GITHUB_TOKEN "your_token_here"
    echo.
)

REM 运行Python脚本
echo.
echo 开始自动化部署流程...
echo.

python automate_android.py %*

if errorlevel 1 (
    echo.
    echo ========================================
    echo 部署失败!
    echo ========================================
    pause
    exit /b 1
) else (
    echo.
    echo ========================================
    echo 部署成功完成!
    echo ========================================
)

pause