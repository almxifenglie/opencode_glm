@echo off
chcp 65001 >nul
title Android设备连接诊断工具
cls

echo ========================================
echo Android设备连接诊断工具
echo ========================================
echo.

echo 步骤1: 检查adb工具
if exist "platform-tools\platform-tools\adb.exe" (
    echo [OK] adb工具已找到
) else (
    echo [ERROR] adb工具未找到
    pause
    exit /b 1
)

echo.
echo 步骤2: 重启adb服务
platform-tools\platform-tools\adb.exe kill-server
platform-tools\platform-tools\adb.exe start-server
echo [OK] adb服务已重启

echo.
echo 步骤3: 检测设备连接
echo 正在检测...
platform-tools\platform-tools\adb.exe devices -l
echo.

echo 步骤4: 等待设备连接
echo 如果设备未显示，请按以下步骤操作：
echo.
echo 【手机端】
echo 1. 设置 → 关于手机 → 连续点击"版本号"7次
echo 2. 设置 → 开发者选项 → 开启"USB调试"
echo 3. 连接USB后，手机弹出"允许USB调试"对话框
echo 4. 勾选"始终允许"，点击"确定"
echo 5. 下拉通知栏，USB模式选择"文件传输"
echo.
echo 【电脑端】
echo 1. 打开设备管理器 (Win+X → 设备管理器)
echo 2. 查看"Android Device"是否有黄色感叹号
echo 3. 如有感叹号，需要安装手机驱动
echo.
echo 按任意键开始60秒等待设备连接...
pause >nul

echo.
echo 正在等待设备连接 (最多60秒)...
timeout /t 60 >nul

echo.
echo 再次检测设备...
platform-tools\platform-tools\adb.exe devices -l

echo.
echo ========================================
echo 如果仍然没有检测到设备：
echo 1. 尝试换一根USB数据线
echo 2. 尝试换一个USB接口
echo 3. 尝试在另一台电脑测试
echo 4. 安装手机官方驱动
echo ========================================
pause