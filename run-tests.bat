@echo off
echo ========================================
echo EV Swap Project - Test Runner
echo ========================================

echo.
echo [1/4] Cleaning previous test results...
call mvn clean

echo.
echo [2/4] Compiling project...
call mvn compile

if %ERRORLEVEL% neq 0 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo.
echo [3/4] Running unit tests...
call mvn test

if %ERRORLEVEL% neq 0 (
    echo ERROR: Unit tests failed!
    pause
    exit /b 1
)

echo.
echo [4/4] Generating test coverage report...
call mvn jacoco:report

echo.
echo ========================================
echo Test execution completed successfully!
echo ========================================
echo.
echo Test reports available at:
echo - target/site/jacoco/index.html (Coverage Report)
echo - target/surefire-reports/ (Test Results)
echo.
pause
