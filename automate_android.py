#!/usr/bin/env python3
"""
Android自动化脚本
功能：
1. 检测代码变化
2. 触发GitHub Actions构建
3. 下载APK
4. 通过adb安装到设备
5. 启动应用
"""

import os
import sys
import time
import requests
import subprocess
import json
from pathlib import Path
import argparse
from typing import Optional, Dict, List
import re

# 配置
GITHUB_REPO = "almxifenglie/opencode_glm"
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN", "")
WORKFLOW_ID = "android.yml"
ADB_PATH = "platform-tools/platform-tools/adb.exe"
APK_NAME = "app-debug.apk"

class AndroidAutomator:
    def __init__(self, repo: str, token: str = ""):
        self.repo = repo
        self.token = token
        self.headers = {
            "Accept": "application/vnd.github.v3+json",
            "Authorization": f"token {token}" if token else ""
        }
        self.api_base = f"https://api.github.com/repos/{repo}"
        
    def get_latest_workflow_run(self) -> Optional[Dict]:
        """获取最新的workflow运行状态"""
        url = f"{self.api_base}/actions/workflows/{WORKFLOW_ID}/runs"
        try:
            response = requests.get(url, headers=self.headers)
            response.raise_for_status()
            runs = response.json()["workflow_runs"]
            return runs[0] if runs else None
        except Exception as e:
            print(f"获取workflow运行失败: {e}")
            return None
    
    def trigger_workflow(self) -> Optional[Dict]:
        """手动触发workflow"""
        url = f"{self.api_base}/actions/workflows/{WORKFLOW_ID}/dispatches"
        data = {
            "ref": "main",
            "inputs": {}
        }
        try:
            response = requests.post(url, headers=self.headers, json=data)
            response.raise_for_status()
            print("Workflow触发成功")
            time.sleep(5)  # 等待workflow启动
            return self.get_latest_workflow_run()
        except Exception as e:
            print(f"触发workflow失败: {e}")
            return None
    
    def wait_for_completion(self, run_id: int, timeout: int = 600) -> bool:
        """等待workflow完成"""
        start_time = time.time()
        url = f"{self.api_base}/actions/runs/{run_id}"
        
        while time.time() - start_time < timeout:
            try:
                response = requests.get(url, headers=self.headers)
                response.raise_for_status()
                run = response.json()
                status = run["status"]
                conclusion = run.get("conclusion")
                
                print(f"状态: {status}, 结论: {conclusion}")
                
                if status == "completed":
                    return conclusion == "success"
                
                time.sleep(10)  # 每10秒检查一次
                
            except Exception as e:
                print(f"检查workflow状态失败: {e}")
                time.sleep(5)
        
        print("等待超时")
        return False
    
    def download_artifact(self, run_id: int, output_dir: str = "downloads") -> Optional[str]:
        """下载workflow产物"""
        # 获取artifacts列表
        url = f"{self.api_base}/actions/runs/{run_id}/artifacts"
        try:
            response = requests.get(url, headers=self.headers)
            response.raise_for_status()
            artifacts = response.json()["artifacts"]
            
            if not artifacts:
                print("没有找到artifacts")
                return None
            
            # 找到app-debug artifact
            artifact = next((a for a in artifacts if a["name"] == "app-debug"), None)
            if not artifact:
                print("没有找到app-debug artifact")
                return None
            
            # 下载artifact
            download_url = artifact["archive_download_url"]
            response = requests.get(download_url, headers=self.headers, stream=True)
            response.raise_for_status()
            
            # 创建下载目录
            Path(output_dir).mkdir(exist_ok=True)
            zip_path = Path(output_dir) / f"artifacts_{run_id}.zip"
            
            with open(zip_path, "wb") as f:
                for chunk in response.iter_content(chunk_size=8192):
                    f.write(chunk)
            
            print(f"Artifact下载完成: {zip_path}")
            return str(zip_path)
            
        except Exception as e:
            print(f"下载artifact失败: {e}")
            return None
    
    def extract_and_install(self, zip_path: str, device_id: str = "") -> bool:
        """解压并安装APK"""
        import zipfile
        
        try:
            # 解压ZIP
            extract_dir = Path(zip_path).parent / Path(zip_path).stem
            extract_dir.mkdir(exist_ok=True)
            
            with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                zip_ref.extractall(extract_dir)
            
            # 查找APK文件
            apk_path = None
            for root, dirs, files in os.walk(extract_dir):
                for file in files:
                    if file.endswith(".apk"):
                        apk_path = Path(root) / file
                        break
                if apk_path:
                    break
            
            if not apk_path:
                print("在ZIP文件中没有找到APK")
                return False
            
            print(f"找到APK: {apk_path}")
            
            # 检查adb
            if not os.path.exists(ADB_PATH):
                print(f"adb路径不存在: {ADB_PATH}")
                print("请确保已安装Android SDK platform-tools")
                return False
            
            # 构建adb命令
            adb_cmd = [ADB_PATH]
            if device_id:
                adb_cmd.extend(["-s", device_id])
            
            # 检查设备连接
            check_cmd = adb_cmd + ["devices"]
            result = subprocess.run(check_cmd, capture_output=True, text=True)
            print(f"设备列表:\n{result.stdout}")
            
            if "device" not in result.stdout:
                print("没有找到已连接的设备")
                return False
            
            # 安装APK
            print("正在安装APK...")
            install_cmd = adb_cmd + ["install", "-r", str(apk_path)]
            install_result = subprocess.run(install_cmd, capture_output=True, text=True)
            
            if install_result.returncode == 0:
                print("APK安装成功")
                
                # 启动应用
                print("启动应用...")
                launch_cmd = adb_cmd + ["shell", "monkey", "-p", "com.fund.arb", "-c", "android.intent.category.LAUNCHER", "1"]
                subprocess.run(launch_cmd)
                
                # 显示日志
                print("查看应用日志:")
                log_cmd = adb_cmd + ["logcat", "-s", "System.out:*", "--pid=$(adb shell pidof com.fund.arb)", "-T", "50"]
                subprocess.run(log_cmd, timeout=10)
                
                return True
            else:
                print(f"APK安装失败: {install_result.stderr}")
                return False
                
        except Exception as e:
            print(f"安装APK失败: {e}")
            return False
    
    def run_full_automation(self, trigger_new: bool = True, device_id: str = "") -> bool:
        """运行完整自动化流程"""
        print("=" * 50)
        print("开始Android自动化流程")
        print("=" * 50)
        
        # 1. 获取或触发workflow
        if trigger_new:
            print("触发新的workflow构建...")
            run = self.trigger_workflow()
        else:
            print("获取最新的workflow运行...")
            run = self.get_latest_workflow_run()
        
        if not run:
            print("无法获取workflow运行")
            return False
        
        run_id = run["id"]
        print(f"Workflow运行ID: {run_id}")
        
        # 2. 等待构建完成
        print("等待构建完成...")
        if not self.wait_for_completion(run_id):
            print("构建失败或超时")
            return False
        
        print("构建成功完成!")
        
        # 3. 下载产物
        print("下载构建产物...")
        zip_path = self.download_artifact(run_id)
        if not zip_path:
            print("下载产物失败")
            return False
        
        # 4. 安装并运行
        print("安装APK到设备...")
        return self.extract_and_install(zip_path, device_id)

def main():
    parser = argparse.ArgumentParser(description="Android自动化构建和部署")
    parser.add_argument("--token", help="GitHub Personal Access Token")
    parser.add_argument("--device", help="指定设备ID (adb -s <device_id>)")
    parser.add_argument("--no-trigger", action="store_true", help="不触发新构建，使用最新的")
    parser.add_argument("--check-only", action="store_true", help="只检查状态，不安装")
    
    args = parser.parse_args()
    
    # 获取token
    token = args.token or GITHUB_TOKEN
    if not token:
        print("警告: 没有提供GitHub Token，某些API可能受限")
        print("请设置环境变量 GITHUB_TOKEN 或使用 --token 参数")
    
    # 创建automator
    automator = AndroidAutomator(GITHUB_REPO, token)
    
    if args.check_only:
        # 只检查状态
        run = automator.get_latest_workflow_run()
        if run:
            print(f"最新workflow运行:")
            print(f"  ID: {run['id']}")
            print(f"  状态: {run['status']}")
            print(f"  结论: {run.get('conclusion', 'N/A')}")
            print(f"  时间: {run['created_at']}")
        return
    
    # 运行完整流程
    success = automator.run_full_automation(
        trigger_new=not args.no_trigger,
        device_id=args.device
    )
    
    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()