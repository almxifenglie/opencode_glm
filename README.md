# LOF/QDII 套利监控系统

一个用于监控 LOF 和 QDII 基金溢价率的系统，帮助发现套利机会。

## 功能特性

- **溢价率排行**: QDII/LOF 按溢价率排序，快速发现套利机会
- **申购状态监控**: 显示基金是否开放申购及申购限额
- **实时净值估算**: 盘中根据指数涨幅估算 LOF 净值
- **自选基金**: 收藏关注的基金，快速查看
- **手机适配**: H5 响应式页面，手机浏览器直接使用

## 技术栈

- 后端: Python FastAPI + SQLite
- 前端: Vue 3 + Vite
- 数据源: AkShare (免费开源)

## 快速开始

### 1. 安装后端依赖

```bash
cd backend
pip install -r requirements.txt
```

### 2. 安装前端依赖

```bash
cd frontend
npm install
```

### 3. 启动后端服务

```bash
cd backend
python -m app.main
```

后端服务运行在 `http://localhost:8000`

### 4. 启动前端服务

```bash
cd frontend
npm run dev
```

前端服务运行在 `http://localhost:3000`

### 5. 访问应用

打开浏览器访问 `http://localhost:3000`

## API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/qdii/ranking` | GET | QDII 溢价率排行 |
| `/api/lof/ranking` | GET | LOF 溢价率排行 |
| `/api/all/ranking` | GET | 综合排行榜 |
| `/api/fund/{code}` | GET | 基金详情 |
| `/api/purchase/{code}` | GET | 申购状态 |
| `/api/search?keyword=xxx` | GET | 搜索基金 |

## 数据更新

- 数据每 3 分钟自动刷新一次
- 手动刷新: 点击右上角刷新按钮

## 手机访问

### 方法一: 局域网访问
1. 确保手机和电脑在同一 WiFi 网络
2. 查看电脑 IP 地址: `ipconfig` (Windows) 或 `ifconfig` (Mac/Linux)
3. 手机浏览器访问: `http://电脑IP:3000`

### 方法二: 内网穿透
使用 ngrok 或 frp 等工具将本地服务暴露到公网。

```bash
# 使用 ngrok
ngrok http 3000
```

## 目录结构

```
opencode_glm/
├── backend/                # 后端服务
│   ├── app/
│   │   ├── main.py        # FastAPI 入口
│   │   ├── config.py      # 配置
│   │   ├── database.py    # 数据库
│   │   ├── models.py      # 数据模型
│   │   ├── api/           # API 路由
│   │   ├── services/      # 数据采集
│   │   └── scheduler/     # 定时任务
│   ├── data/              # 数据库文件
│   └── requirements.txt
│
└── frontend/              # 前端 H5
    ├── src/
    │   ├── views/         # 页面组件
    │   ├── api/           # API 调用
    │   └── style.css      # 样式
    ├── package.json
    └── vite.config.js
```

## 注意事项

1. 数据仅供参考，不构成投资建议
2. AkShare 数据来源于公开接口，请遵守使用频率限制
3. 套利有风险，需考虑交易成本、折溢价变化等因素

## License

MIT
