@echo off
setlocal enabledelayedexpansion

echo ========================= Health Check Test ========================= > health_check_report.txt
echo Test Date: %date% %time% >> health_check_report.txt
echo ================================================================== >> health_check_report.txt
echo. >> health_check_report.txt

set "all_healthy=true"
set "base_url=http://localhost:8080/api"

echo Running Basic Health Check test...
echo [TEST] Basic Health Check >> health_check_report.txt

curl -s "%base_url%/test/health" -o basic_health.json

:: Parse JSON and extract the is_healthy field
for /f "tokens=2 delims=:," %%a in ('findstr "is_healthy" basic_health.json') do (
    set "basic_health=%%a"
    set "basic_health=!basic_health: =!"
    set "basic_health=!basic_health:"=!"
)

if "!basic_health!"=="true" (
    echo [PASS] Basic Health Check - All systems healthy >> health_check_report.txt
) else (
    echo [FAIL] Basic Health Check - One or more systems unhealthy >> health_check_report.txt
    set "all_healthy=false"
)

:: Add API response to report
echo. >> health_check_report.txt
echo [API Response] Basic Health Check >> health_check_report.txt
echo ------------------------------------------------------------------ >> health_check_report.txt
type basic_health.json >> health_check_report.txt
echo. >> health_check_report.txt
echo ------------------------------------------------------------------ >> health_check_report.txt
echo. >> health_check_report.txt

echo Running Detailed Health Check test...
echo [TEST] Detailed Health Check >> health_check_report.txt

curl -s "%base_url%/test/health/detail" -o detailed_health.json

:: Parse JSON and extract the is_healthy field
for /f "tokens=2 delims=:," %%a in ('findstr "is_healthy" detailed_health.json') do (
    set "detailed_health=%%a"
    set "detailed_health=!detailed_health: =!"
    set "detailed_health=!detailed_health:"=!"
)

if "!detailed_health!"=="true" (
    echo [PASS] Detailed Health Check - All systems healthy >> health_check_report.txt
) else (
    echo [FAIL] Detailed Health Check - One or more systems unhealthy >> health_check_report.txt
    set "all_healthy=false"
)

:: Add API response to report
echo. >> health_check_report.txt
echo [API Response] Detailed Health Check >> health_check_report.txt
echo ------------------------------------------------------------------ >> health_check_report.txt
type detailed_health.json >> health_check_report.txt
echo. >> health_check_report.txt
echo ------------------------------------------------------------------ >> health_check_report.txt
echo. >> health_check_report.txt

echo ========================= Summary ========================= >> health_check_report.txt

if "!all_healthy!"=="true" (
    echo [OVERALL RESULT] PASS - All health checks passed >> health_check_report.txt
    echo Health check passed. All systems are operational.
) else (
    echo [OVERALL RESULT] FAIL - One or more health checks failed >> health_check_report.txt
    echo Health check failed. Please check the report for details.
)

echo ================================================================== >> health_check_report.txt
echo. >> health_check_report.txt
echo Detailed results saved to health_check_report.txt

:: Clean up temp files
del basic_health.json
del detailed_health.json

echo.
echo Press any key to exit...
pause > nul

endlocal 