#!/usr/bin/env python3
"""
简化版Android自动化脚本
直接从GitHub Actions页面获取最新构建
"""

import os
import sys
import time
import subprocess
import webbrowser
from pathlib import Path
import urllib.request
import zipfile

def check_adb():
    """检查adb是否可用"""
    adb_path = Path("platform-tools/platform-tools/adb.exe")
    if adb_path.exists():
        print(f"[OK] adb找到: {adb_path}")
        return True
    else:
        print(f"[ERROR] adb未找到: {adb_path}")
        return False

def check_device():
    """检查设备连接"""
    adb_path = Path("platform-tools/platform-tools/adb.exe")
    try:
        result = subprocess.run([str(adb_path), "devices"], 
                              capture_output=True, text=True, timeout=5)
        lines = result.stdout.strip().split('\n')
        
        if len(lines) > 1:
            devices = []
            for line in lines[1:]:
                if line.strip() and "device" in line:
                    device_id = line.split('\t')[0]
                    devices.append(device_id)
            
            if devices:
                print(f"[OK] 找到设备: {devices}")
                return True
            else:
                print("[ERROR] 找到设备但没有'device'状态")
        else:
            print("[ERROR] 没有连接的设备")
            
        print("请确保:")
        print("1. USB线已连接")
        print("2. 开启开发者选项")
        print("3. 开启USB调试")
        print("4. 授权电脑调试")
        return False
        
    except Exception as e:
        print(f"❌ 检查设备失败: {e}")
        return False

def download_latest_apk():
    """下载最新APK"""
    print("📥 正在从GitHub Actions下载最新APK...")
    
    # GitHub Actions下载URL（需要手动从页面获取）
    actions_url = "https://github.com/almxifenglie/opencode_glm/actions"
    
    print(f"请手动下载APK:")
    print(f"1. 打开: {actions_url}")
    print(f"2. 点击最新的构建记录")
    print(f"3. 滚动到 Artifacts 部分")
    print(f"4. 下载 app-debug")
    
    webbrowser.open(actions_url)
    
    # 等待用户下载
    input("请下载APK到当前目录，按Enter继续...")
    
    # 查找APK文件
    download_dir = Path(".") / "downloads"
    apk_files = list(download_dir.glob("*.apk")) if download_dir.exists() else []
    
    if not apk_files:
        # 在当前目录查找
        apk_files = list(Path(".").glob("*.apk"))
    
    if apk_files:
        print(f"✅ 找到APK: {apk_files[0]}")
        return str(apk_files[0])
    else:
        print("❌ 没有找到APK文件")
        return None

def install_and_run(apk_path):
    """安装并运行APK"""
    adb_path = Path("platform-tools/platform-tools/adb.exe")
    
    print(f"📱 正在安装APK: {apk_path}")
    
    try:
        # 卸载旧版本（可选）
        print("正在卸载旧版本...")
        subprocess.run([str(adb_path), "uninstall", "com.fund.arb"], 
                      capture_output=True, timeout=10)
        
        # 安装APK
        print("正在安装新版本...")
        result = subprocess.run([str(adb_path), "install", "-r", apk_path], 
                              capture_output=True, text=True, timeout=30)
        
        if result.returncode == 0:
            print("✅ APK安装成功")
            
            # 启动应用
            print("🚀 启动应用...")
            subprocess.run([str(adb_path), "shell", "monkey", "-p", "com.fund.arb", 
                          "-c", "android.intent.category.LAUNCHER", "1"], 
                         capture_output=True, timeout=5)
            
            # 等待应用启动
            time.sleep(3)
            
            # 显示日志
            print("\n📋 应用日志:")
            print("-" * 50)
            
            # 获取应用PID
            pid_result = subprocess.run([str(adb_path), "shell", "pidof", "com.fund.arb"], 
                                       capture_output=True, text=True, timeout=5)
            
            if pid_result.returncode == 0 and pid_result.stdout.strip():
                pid = pid_result.stdout.strip()
                print(f"应用PID: {pid}")
                
                # 查看日志（带过滤）
                print("过滤日志中...")
                subprocess.run([str(adb_path), "logcat", "--pid", pid, "-T", "20", 
                              "System.out:*", "*:E"], 
                             timeout=10)
            else:
                print("正在查看系统日志...")
                subprocess.run([str(adb_path), "logcat", "-s", "System.out:*", "-T", "30"], 
                             timeout=10)
            
            print("-" * 50)
            return True
        else:
            print(f"❌ APK安装失败: {result.stderr}")
            return False
            
    except Exception as e:
        print(f"❌ 安装失败: {e}")
        return False

def manual_debug_mode():
    """手动调试模式"""
    print("\n🔧 手动调试模式")
    print("=" * 50)
    
    adb_path = Path("platform-tools/platform-tools/adb.exe")
    
    while True:
        print("\n选项:")
        print("1. 查看设备列表")
        print("2. 查看应用日志")
        print("3. 清除应用数据")
        print("4. 启动应用")
        print("5. 停止应用")
        print("6. 查看网络请求")
        print("7. 返回主菜单")
        
        choice = input("\n请输入选项 (1-7): ").strip()
        
        if choice == "1":
            subprocess.run([str(adb_path), "devices", "-l"])
            
        elif choice == "2":
            print("查看日志中... (Ctrl+C退出)")
            try:
                subprocess.run([str(adb_path), "logcat", "-s", "System.out:*", "*:E"])
            except KeyboardInterrupt:
                print("\n日志查看已停止")
                
        elif choice == "3":
            subprocess.run([str(adb_path), "shell", "pm", "clear", "com.fund.arb"])
            print("应用数据已清除")
            
        elif choice == "4":
            subprocess.run([str(adb_path), "shell", "monkey", "-p", "com.fund.arb", 
                          "-c", "android.intent.category.LAUNCHER", "1"])
            print("应用已启动")
            
        elif choice == "5":
            subprocess.run([str(adb_path), "shell", "am", "force-stop", "com.fund.arb"])
            print("应用已停止")
            
        elif choice == "6":
            print("网络请求日志:")
            subprocess.run([str(adb_path), "logcat", "-s", "OkHttp:*", "Retrofit:*"])
            
        elif choice == "7":
            break
            
        else:
            print("无效选项")

def main():
    print("Android自动化部署工具")
    print("=" * 50)
    
    # 检查环境
    if not check_adb():
        print("\n正在下载platform-tools...")
        try:
            # 下载并解压platform-tools
            url = "https://dl.google.com/android/repository/platform-tools-latest-windows.zip"
            print(f"下载: {url}")
            
            import urllib.request
            urllib.request.urlretrieve(url, "platform-tools.zip")
            
            import zipfile
            with zipfile.ZipFile("platform-tools.zip", 'r') as zip_ref:
                zip_ref.extractall(".")
            
            print("✅ platform-tools下载完成")
        except Exception as e:
            print(f"❌ 下载失败: {e}")
            return
    
    if not check_device():
        print("\n请连接设备后重试")
        return
    
    while True:
        print("\n主菜单:")
        print("1. 自动下载并安装最新APK")
        print("2. 手动选择APK文件安装")
        print("3. 手动调试模式")
        print("4. 退出")
        
        choice = input("\n请输入选项 (1-4): ").strip()
        
        if choice == "1":
            apk_path = download_latest_apk()
            if apk_path:
                install_and_run(apk_path)
                
        elif choice == "2":
            apk_path = input("请输入APK文件路径: ").strip()
            if os.path.exists(apk_path):
                install_and_run(apk_path)
            else:
                print("❌ 文件不存在")
                
        elif choice == "3":
            manual_debug_mode()
            
        elif choice == "4":
            print("再见!")
            break
            
        else:
            print("无效选项")

if __name__ == "__main__":
    main()