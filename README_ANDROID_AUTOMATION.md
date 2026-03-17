# Android自动化构建部署

自动化脚本可以自动触发GitHub Actions构建、下载APK并安装到设备。

## 功能特性

✅ **自动触发构建** - 代码提交后自动触发或手动触发  
✅ **监控构建状态** - 实时监控GitHub Actions状态  
✅ **自动下载APK** - 构建完成后自动下载产物  
✅ **自动安装部署** - 通过adb安装到连接的Android设备  
✅ **自动启动应用** - 安装完成后自动启动应用  
✅ **日志查看** - 实时查看应用日志输出  

## 使用要求

### 系统要求
- Python 3.6+
- Windows/Linux/macOS
- Git
- GitHub账号（需要Personal Access Token）

### 环境准备

1. **安装Python依赖**
```bash
pip install requests
```

2. **设置GitHub Token**
```bash
# Windows
setx GITHUB_TOKEN "your_personal_access_token_here"

# Linux/macOS
export GITHUB_TOKEN="your_personal_access_token_here"
```

在GitHub创建Personal Access Token：
- 访问 https://github.com/settings/tokens
- 点击 "Generate new token"
- 选择权限：`repo`、`workflow`
- 复制生成的token

3. **连接Android设备**
- 开启开发者选项
- 开启USB调试
- 连接USB线
- 授权电脑调试

## 使用方法

### 基本使用（Windows）
```bash
auto_deploy.bat
```

### Python脚本直接使用
```bash
# 触发新构建并安装
python automate_android.py

# 使用已有构建（不触发新构建）
python automate_android.py --no-trigger

# 指定设备ID
python automate_android.py --device <device_id>

# 只检查状态不安装
python automate_android.py --check-only

# 手动指定token
python automate_android.py --token your_token_here
```

### 获取设备ID
```bash
platform-tools/platform-tools/adb.exe devices -l
```

## 自动化流程

脚本执行以下步骤：

1. **检查状态** - 获取最新workflow运行状态
2. **触发构建** - 如果需要则触发新构建（可选）
3. **等待完成** - 监控GitHub Actions直到完成（最长10分钟）
4. **下载APK** - 从GitHub Actions下载构建产物
5. **安装应用** - 通过adb安装到设备
6. **启动应用** - 自动启动应用
7. **查看日志** - 显示应用日志输出

## 调试和排错

### 常见问题

**Q: 无法找到设备**
```
A: 确保：
   1. 设备已连接USB
   2. 已开启USB调试
   3. 电脑已授权调试
   4. 运行 `adb devices` 确认设备列表
```

**Q: GitHub API限制**
```
A: 设置GITHUB_TOKEN环境变量
   未设置token时API调用有限制（60次/小时）
   设置token后可提高到5000次/小时
```

**Q: 构建失败**
```
A: 检查：
   1. 代码编译错误（查看GitHub Actions日志）
   2. 网络连接问题
   3. API请求失败
```

**Q: 下载失败**
```
A: 检查：
   1. 网络连接
   2. GitHub权限
   3. 磁盘空间
```

### 查看详细日志

脚本使用Python的`print()`输出日志，包含：
- API调用状态
- 构建进度
- 下载状态
- 安装结果
- 设备连接状态

### 手动调试步骤

```bash
# 1. 检查设备连接
platform-tools/platform-tools/adb.exe devices

# 2. 手动安装APK
platform-tools/platform-tools/adb.exe install -r downloads/app-debug.apk

# 3. 查看应用日志
platform-tools/platform-tools/adb.exe logcat -s System.out:* --pid=$(adb shell pidof com.fund.arb)
```

## 集成到开发流程

### 自动构建配置（推荐）

在`.github/workflows/android.yml`中添加触发条件：

```yaml
name: Build Android APK

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:  # 允许手动触发
  schedule:
    - cron: '0 */6 * * *'  # 每6小时自动构建
```

### CI/CD流水线示例

```bash
# 1. 开发阶段
git commit -m "fix: some fix"
git push origin main

# 2. 自动触发构建（GitHub Actions自动运行）

# 3. 自动部署到测试设备
python automate_android.py --no-trigger

# 4. 测试验证
# 脚本自动启动应用并显示日志
```

## 高级功能

### 自定义配置

编辑`automate_android.py`中的常量：

```python
# 配置
GITHUB_REPO = "your_username/your_repo"  # 修改为你的仓库
WORKFLOW_ID = "android.yml"  # workflow文件名
APK_NAME = "app-debug.apk"  # APK文件名
ADB_PATH = "platform-tools/platform-tools/adb.exe"  # adb路径
```

### 扩展功能

脚本设计为可扩展，可以添加：

- **邮件通知** - 构建完成后发送邮件
- **多设备部署** - 同时安装到多个设备
- **版本管理** - 自动版本号和changelog
- **测试自动化** - 运行UI测试
- **发布渠道** - 分发到不同渠道（测试/生产）

## 注意事项

1. **安全** - 不要将GitHub Token提交到代码仓库
2. **网络** - 需要稳定网络连接访问GitHub API
3. **设备** - 确保设备始终连接
4. **权限** - 确保有足够的GitHub权限
5. **备份** - 重要数据定期备份

## 技术支持

遇到问题请提供：
1. 完整的错误日志
2. 操作系统版本
3. Python版本
4. 设备型号和Android版本
5. 网络环境信息