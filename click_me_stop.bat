@echo off
echo Stopping Spring Boot Application and Docker containers...

REM Check if Docker is running
docker info > nul 2>&1
if %errorlevel% equ 0 (
    echo Stopping Docker containers...
    docker-compose down
) else (
    echo Docker is not running.
)

echo.
echo All services stopped.
pause 