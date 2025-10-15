#!/bin/bash

echo "========================================"
echo "EV Swap Project - Test Runner"
echo "========================================"

echo ""
echo "[1/4] Cleaning previous test results..."
mvn clean

echo ""
echo "[2/4] Compiling project..."
mvn compile

if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed!"
    exit 1
fi

echo ""
echo "[3/4] Running unit tests..."
mvn test

if [ $? -ne 0 ]; then
    echo "ERROR: Unit tests failed!"
    exit 1
fi

echo ""
echo "[4/4] Generating test coverage report..."
mvn jacoco:report

echo ""
echo "========================================"
echo "Test execution completed successfully!"
echo "========================================"
echo ""
echo "Test reports available at:"
echo "- target/site/jacoco/index.html (Coverage Report)"
echo "- target/surefire-reports/ (Test Results)"
echo ""
