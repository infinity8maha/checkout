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
echo Checking Gradle Wrapper components...

REM Check if Gradle Wrapper jar exists
if not exist ".gradle\wrapper\gradle-wrapper.jar" (
    echo Gradle Wrapper components not found. Installing...
    call gradlew.bat --version > nul 2>&1
    if %errorlevel% neq 0 (
        echo Failed to install Gradle Wrapper components. Trying to download...
        call gradle wrapper --gradle-version 8.2
    ) else (
        echo Gradle Wrapper components installed successfully.
    )
) else (
    echo Gradle Wrapper components already installed.
)

echo.
echo Running Spring Boot application...
echo You can access the application at: http://localhost:8080/api/swagger-ui.html
echo Press Ctrl+C to stop the application when you want to exit.
echo.

REM Suppress all progress indicators and execution percentage
call gradlew.bat bootRun --console=plain --quiet

pause 