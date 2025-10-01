@echo off
setlocal

rem Change to the directory of this script (project root)
cd /d "%~dp0"

set FE_PORT=%~1
if "%FE_PORT%"=="" set FE_PORT=5173

echo Launching backend...
start "EVSWAP Backend" cmd /c ".\mvnw.cmd spring-boot:run"

echo Launching frontend on port %FE_PORT%...
pushd FrontEnd
if exist run-frontend.bat (
  start "EVSWAP Frontend" cmd /c ".\run-frontend.bat %FE_PORT%"
) else (
  start "EVSWAP Frontend" cmd /c "npx --yes http-server -a 127.0.0.1 -p %FE_PORT% -d -c-1 ."
)
popd

echo Waiting for services to start...
timeout /t 5 >nul

echo Backend: http://127.0.0.1:8080
echo Frontend: http://127.0.0.1:%FE_PORT%/landing.html

endlocal


