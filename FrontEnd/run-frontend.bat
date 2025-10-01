@echo off
setlocal

rem Allow overriding port via first argument (default 5173)
set PORT=%~1
if "%PORT%"=="" set PORT=5173

rem Root directory of this script
set "ROOT=%~dp0"
echo Serving folder: %ROOT%
echo Target: http://127.0.0.1:%PORT%

rem Try global http-server first
where http-server >nul 2>nul
if %ERRORLEVEL%==0 (
  start "EVSWAP FE Server" /D "%ROOT%" cmd /c "http-server -a 127.0.0.1 -p %PORT% -d -c-1 ."
) else (
  rem Fallback to npx http-server
  start "EVSWAP FE Server" /D "%ROOT%" cmd /c "npx --yes http-server -a 127.0.0.1 -p %PORT% -d -c-1 ."
)

rem Wait a moment for server
timeout /t 4 >nul

if exist "%ROOT%landing.html" (
  start "" http://127.0.0.1:%PORT%/landing.html
) else (
  start "" http://127.0.0.1:%PORT%/
)

endlocal
