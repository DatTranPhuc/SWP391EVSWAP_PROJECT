@echo off
setlocal

rem Change to the directory of this script (project root)
cd /d "%~dp0"

echo Starting Spring Boot backend with Maven Wrapper...
start "EVSWAP Backend" cmd /c ".\mvnw.cmd spring-boot:run"

echo Backend starting on http://127.0.0.1:8080

endlocal


