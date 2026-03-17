@echo off
chcp 65001 >nul
echo ========================================
echo Android自动化调试脚本
echo ========================================
echo.

REM 设置adb路径
set ADB_PATH=platform-tools\platform-tools\adb.exe

REM 检查adb
if not exist "%ADB_PATH%" (
    echo [ERROR] adb未找到: %ADB_PATH%
    echo 正在下载platform-tools...
    
    powershell -Command "Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/platform-tools-latest-windows.zip' -OutFile 'platform-tools.zip' -UseBasicParsing"
    if errorlevel 1 (
        echo [ERROR] 下载失败
        pause
        exit /b 1
    )
    
    powershell -Command "Expand-Archive -Path 'platform-tools.zip' -DestinationPath '.' -Force"
    echo [OK] platform-tools下载完成
    echo.
)

REM 检查设备
echo 检查设备连接...
"%ADB_PATH%" devices > temp_devices.txt
findstr /c:"device" temp_devices.txt >nul
if errorlevel 1 (
    echo [ERROR] 没有找到已连接的设备
    echo.
    echo 请确保:
    echo 1. USB线已连接
    echo 2. 开启开发者选项
    echo 3. 开启USB调试
    echo 4. 授权电脑调试
    echo.
    del temp_devices.txt
    pause
    exit /b 1
)

echo [OK] 设备连接正常
echo.
type temp_devices.txt
echo.
del temp_devices.txt

REM 下载最新APK
echo 下载最新APK...
echo 请从GitHub Actions下载最新APK:
echo https://github.com/almxifenglie/opencode_glm/actions
echo.
echo 将APK文件放在当前目录或downloads文件夹
echo.
set /p APK_PATH="输入APK文件路径（直接回车使用默认）: "
if "%APK_PATH%"=="" (
    REM 查找APK文件
    if exist "downloads\*.apk" (
        for %%f in (downloads\*.apk) do set APK_PATH=%%f
    ) else if exist "*.apk" (
        for %%f in (*.apk) do set APK_PATH=%%f
    )
)

if not exist "%APK_PATH%" (
    echo [ERROR] APK文件不存在: %APK_PATH%
    pause
    exit /b 1
)

echo [OK] 使用APK: %APK_PATH%
echo.

REM 安装APK
echo 正在安装APK...
"%ADB_PATH%" install -r "%APK_PATH%"
if errorlevel 1 (
    echo [ERROR] 安装失败
    pause
    exit /b 1
)

echo [OK] APK安装成功
echo.

REM 启动应用
echo 启动应用...
"%ADB_PATH%" shell monkey -p com.fund.arb -c android.intent.category.LAUNCHER 1
timeout /t 3 >nul

REM 显示应用信息
echo.
echo 应用信息:
echo ==========
"%ADB_PATH%" shell dumpsys package com.fund.arb | findstr "versionName versionCode"
echo.

REM 查看日志
echo 查看应用日志（Ctrl+C退出）:
echo ========================================
"%ADB_PATH%" logcat -s System.out:* *:E --pid="%adb% shell pidof com.fund.arb" -T 50

echo.
echo ========================================
echo 调试完成
echo ========================================
pause