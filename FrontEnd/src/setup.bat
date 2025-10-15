@echo off
echo ====================================
echo   EV SWAP - Setup Script
echo ====================================
echo.

REM Check Node.js
echo [1/4] Checking Node.js installation...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Node.js is not installed!
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)
echo Node.js: OK
echo.

REM Install dependencies
echo [2/4] Installing dependencies...
call npm install
if %errorlevel% neq 0 (
    echo ERROR: Failed to install dependencies
    pause
    exit /b 1
)
echo Dependencies installed successfully!
echo.

REM Create .env file
echo [3/4] Creating .env file...
if not exist .env (
    copy .env.example .env >nul
    echo .env file created successfully!
) else (
    echo .env file already exists, skipping...
)
echo.

REM Done
echo [4/4] Setup completed!
echo.
echo ====================================
echo   Setup successful! 
echo ====================================
echo.
echo Next steps:
echo 1. Run "npm run dev" to start development server
echo 2. Open http://localhost:3000 in your browser
echo 3. Login with admin@evswap.com / Admin@123456
echo.
echo Happy coding!
echo.
pause
