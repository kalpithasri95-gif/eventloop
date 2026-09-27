@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo        EVENTLOOP - STARTING CLOUD WEB SERVER
echo ========================================================

:: Check for Java
set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Users\lenovo1\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\java.exe" (
        set "JAVA_CMD=C:\Users\lenovo1\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\java.exe"
    ) else (
        echo [ERROR] Java Runtime not found on PATH or default IDE location.
        exit /b 1
    )
)

if not exist bin\com\eventloop\web\EventLoopWebServer.class (
    echo [INFO] Binaries not found, compiling first...
    call compile.bat
    if %errorlevel% neq 0 (
        echo [ERROR] Compilation failed.
        exit /b 1
    )
)

echo Starting EventLoop Web Server on port 8080...
echo.
echo ========================================================
echo Open in your browser:
echo Local:     http://localhost:8080
echo Network:   http://localhost:8080 (Accessible by other devices on same WiFi)
echo Press Ctrl+C in this terminal to stop the server anytime.
echo ========================================================
echo.

"%JAVA_CMD%" -cp "bin;lib\*" com.eventloop.web.EventLoopWebServer

endlocal
