@echo off
chcp 65001 >nul
title Android APK安装调试工具
cls

echo ========================================
echo Android APK安装调试工具
echo ========================================
echo.

:MAIN_MENU
cls
echo 选项:
echo 1. 下载最新APK (需要手动下载)
echo 2. 安装APK到设备
echo 3. 查看设备日志
echo 4. 清理应用数据
echo 5. 启动/停止应用
echo 6. 退出
echo.

set /p choice="请选择 (1-6): "

if "%choice%"=="1" goto DOWNLOAD_APK
if "%choice%"=="2" goto INSTALL_APK
if "%choice%"=="3" goto VIEW_LOGS
if "%choice%"=="4" goto CLEAR_DATA
if "%choice%"=="5" goto APP_CONTROL
if "%choice%"=="6" goto EXIT
goto MAIN_MENU

:DOWNLOAD_APK
cls
echo ========================================
echo 下载最新APK
echo ========================================
echo.
echo 请从GitHub Actions下载最新构建:
echo.
echo URL: https://github.com/almxifenglie/opencode_glm/actions
echo.
echo 步骤:
echo 1. 打开上面的URL
echo 2. 点击最新的构建 (绿色图标)
echo 3. 滚动到底部Artifacts区域
echo 4. 下载 "app-debug"
echo 5. 解压ZIP文件
echo 6. 将APK文件放在当前目录的downloads文件夹
echo.
echo 下载完成后按任意键继续...
pause >nul
goto MAIN_MENU

:INSTALL_APK
cls
echo ========================================
echo 安装APK到设备
echo ========================================
echo.

REM 检查设备
echo 检查设备连接...
platform-tools\platform-tools\adb.exe devices > temp.txt
findstr /c:"device" temp.txt >nul
if errorlevel 1 (
    echo [ERROR] 没有找到已连接的设备
    echo.
    echo 请确保设备已连接并开启USB调试
    echo.
    del temp.txt
    pause
    goto MAIN_MENU
)

echo [OK] 设备连接正常
echo.
type temp.txt
echo.
del temp.txt

REM 查找APK
echo 查找APK文件...
set APK_FOUND=0

if exist "downloads\*.apk" (
    for %%f in (downloads\*.apk) do (
        set APK_PATH=%%f
        set APK_FOUND=1
    )
) else if exist "*.apk" (
    for %%f in (*.apk) do (
        set APK_PATH=%%f
        set APK_FOUND=1
    )
)

if %APK_FOUND%==0 (
    echo [ERROR] 没有找到APK文件
    echo 请先将APK文件放在当前目录或downloads文件夹
    pause
    goto MAIN_MENU
)

echo [OK] 找到APK: %APK_PATH%
echo.

echo 正在安装...
platform-tools\platform-tools\adb.exe install -r "%APK_PATH%"
if errorlevel 1 (
    echo [ERROR] 安装失败
    pause
    goto MAIN_MENU
)

echo [OK] 安装成功!
echo.
echo 正在启动应用...
platform-tools\platform-tools\adb.exe shell monkey -p com.fund.arb -c android.intent.category.LAUNCHER 1
timeout /t 2 >nul

echo.
echo 应用信息:
echo ----------
platform-tools\platform-tools\adb.exe shell dumpsys package com.fund.arb | findstr "versionName versionCode"

echo.
pause
goto MAIN_MENU

:VIEW_LOGS
cls
echo ========================================
echo 查看设备日志
echo ========================================
echo.
echo 日志选项:
echo 1. 查看应用日志 (System.out)
echo 2. 查看错误日志 (*:E)
echo 3. 查看网络请求 (OkHttp)
echo 4. 查看所有日志
echo 5. 返回
echo.

set /p log_choice="请选择: "

if "%log_choice%"=="1" (
    echo 查看应用日志 (Ctrl+C退出)...
    platform-tools\platform-tools\adb.exe logcat -s System.out:* -v time
)
if "%log_choice%"=="2" (
    echo 查看错误日志 (Ctrl+C退出)...
    platform-tools\platform-tools\adb.exe logcat *:E -v time
)
if "%log_choice%"=="3" (
    echo 查看网络请求日志 (Ctrl+C退出)...
    platform-tools\platform-tools\adb.exe logcat -s OkHttp:* Retrofit:*
)
if "%log_choice%"=="4" (
    echo 查看所有日志 (Ctrl+C退出)...
    platform-tools\platform-tools\adb.exe logcat -v time
)

pause
goto MAIN_MENU

:CLEAR_DATA
cls
echo ========================================
echo 清理应用数据
echo ========================================
echo.
echo 这将清除应用的所有数据（登录、设置等）
set /p confirm="确定要清除应用数据吗？ (y/n): "

if /i "%confirm%"=="y" (
    echo 正在清除应用数据...
    platform-tools\platform-tools\adb.exe shell pm clear com.fund.arb
    echo [OK] 应用数据已清除
)

pause
goto MAIN_MENU

:APP_CONTROL
cls
echo ========================================
echo 应用控制
echo ========================================
echo.
echo 1. 启动应用
echo 2. 停止应用
echo 3. 重启应用
echo 4. 返回
echo.

set /p control_choice="请选择: "

if "%control_choice%"=="1" (
    echo 启动应用...
    platform-tools\platform-tools\adb.exe shell monkey -p com.fund.arb -c android.intent.category.LAUNCHER 1
    echo [OK] 应用已启动
)
if "%control_choice%"=="2" (
    echo 停止应用...
    platform-tools\platform-tools\adb.exe shell am force-stop com.fund.arb
    echo [OK] 应用已停止
)
if "%control_choice%"=="3" (
    echo 停止应用...
    platform-tools\platform-tools\adb.exe shell am force-stop com.fund.arb
    timeout /t 1 >nul
    echo 启动应用...
    platform-tools\platform-tools\adb.exe shell monkey -p com.fund.arb -c android.intent.category.LAUNCHER 1
    echo [OK] 应用已重启
)

pause
goto MAIN_MENU

:EXIT
cls
echo 感谢使用！
echo.
pause
exit