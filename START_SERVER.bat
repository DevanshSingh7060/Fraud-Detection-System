
@echo off
title Fraud Detection System

echo.
echo ==================================================
echo          FRAUD DETECTION SYSTEM
echo ==================================================
echo.
echo Starting Java Backend...
echo.

:: Start Java backend in a separate window
start "Fraud Detection Backend" cmd /k "cd /d %~dp0backend && call mvnw.cmd spring-boot:run"

echo.
echo Waiting for backend to initialize...
timeout /t 15 /nobreak >nul

echo.
echo Starting Frontend Server...
echo.

:: Start frontend in a separate window
start "Fraud Detection Frontend" cmd /k "cd /d %~dp0frontend && py -m http.server 5500"

echo.
echo Waiting for frontend to initialize...
timeout /t 3 /nobreak >nul

echo.
echo Opening Fraud Detection Dashboard...
echo.

:: Open dashboard in default browser
start "" "http://localhost:5500"

echo.
echo ==================================================
echo       FRAUD DETECTION SYSTEM STARTED
echo ==================================================
echo.
echo Backend:  http://localhost:8080
echo Frontend: http://localhost:5500
echo.
echo Keep both server windows open.
echo Close the server windows when finished.
echo.
pause
