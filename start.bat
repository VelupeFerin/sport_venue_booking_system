@echo off
echo 启动运动场馆预订系统...

echo.
echo 1. 启动后端服务...
start "Backend" cmd /k "cd backend && mvn spring-boot:run"

echo.
echo 2. 等待后端启动完成...
timeout /t 10 /nobreak

echo.
echo 3. 启动前端服务...
start "Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo 系统启动完成！
echo 前端地址: http://localhost:5173
echo 后端地址: http://localhost:8080
echo.
pause 