#!/bin/bash

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "===================================="
echo "  EV SWAP - Setup Script"
echo "===================================="
echo ""

# Check Node.js
echo -e "${YELLOW}[1/4] Checking Node.js installation...${NC}"
if ! command -v node &> /dev/null; then
    echo -e "${RED}ERROR: Node.js is not installed!${NC}"
    echo "Please install Node.js from https://nodejs.org/"
    exit 1
fi
echo -e "${GREEN}Node.js: OK ($(node --version))${NC}"
echo ""

# Install dependencies
echo -e "${YELLOW}[2/4] Installing dependencies...${NC}"
npm install
if [ $? -ne 0 ]; then
    echo -e "${RED}ERROR: Failed to install dependencies${NC}"
    exit 1
fi
echo -e "${GREEN}Dependencies installed successfully!${NC}"
echo ""

# Create .env file
echo -e "${YELLOW}[3/4] Creating .env file...${NC}"
if [ ! -f .env ]; then
    cp .env.example .env
    echo -e "${GREEN}.env file created successfully!${NC}"
else
    echo -e "${YELLOW}.env file already exists, skipping...${NC}"
fi
echo ""

# Done
echo -e "${YELLOW}[4/4] Setup completed!${NC}"
echo ""
echo "===================================="
echo -e "${GREEN}  Setup successful!${NC}"
echo "===================================="
echo ""
echo "Next steps:"
echo "1. Run 'npm run dev' to start development server"
echo "2. Open http://localhost:3000 in your browser"
echo "3. Login with admin@evswap.com / Admin@123456"
echo ""
echo "Happy coding! 🎉"
echo ""
