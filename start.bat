@echo off
echo ========================================
echo   LOF/QDII 套利监控系统启动脚本
echo ========================================
echo.

echo [1/2] 启动后端服务...
start cmd /k "cd /d %~dp0backend && python -m app.main"

timeout /t 3 /nobreak >nul

echo [2/2] 启动前端服务...
start cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo ========================================
echo   服务已启动！
echo   后端: http://localhost:8000
echo   前端: http://localhost:3000
echo   API文档: http://localhost:8000/docs
echo ========================================
echo.
pause
