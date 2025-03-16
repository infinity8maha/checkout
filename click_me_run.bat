@echo off
echo Starting Spring Boot Application...
echo.

REM Check if Docker is running
docker info > nul 2>&1
if %errorlevel% equ 0 (
    echo Docker is running.
    echo Starting PostgreSQL container...
    docker-compose up -d postgres
    timeout /t 5 /nobreak
) else (
    echo Warning: Docker is not running. Make sure your database is available.
)

echo.
echo Running Spring Boot application...
echo You can access the application at: http://localhost:8080/api/swagger-ui.html
echo Press Ctrl+C to stop the application when you want to exit.
echo.

REM Suppress all progress indicators and execution percentage
call gradlew.bat bootRun --console=plain --quiet

pause 