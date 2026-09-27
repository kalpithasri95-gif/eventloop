@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo        EVENTLOOP - PACKAGING STANDALONE EXECUTABLE
echo ========================================================

set JAR_CMD=jar
where jar >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Users\lenovo1\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\jar.exe" (
        set "JAR_CMD=C:\Users\lenovo1\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\jar.exe"
    ) else (
        echo [ERROR] JDK 'jar' tool not found.
        exit /b 1
    )
)

if not exist bin\com\eventloop\main\EventLoopApp.class (
    echo Binaries not found. Running compilation first...
    call compile.bat
)

echo Creating MANIFEST.MF...
(
echo Manifest-Version: 1.0
echo Main-Class: com.eventloop.main.EventLoopApp
echo Class-Path: lib/sqlite-jdbc-3.45.1.0.jar lib/flatlaf-3.4.1.jar lib/slf4j-api-1.7.36.jar lib/slf4j-simple-1.7.36.jar
echo.
) > MANIFEST.MF

echo Building EventLoop.jar...
"%JAR_CMD%" -cfm EventLoop.jar MANIFEST.MF -C bin .
del MANIFEST.MF 2>nul

if %errorlevel% equ 0 (
    echo [SUCCESS] EventLoop.jar created successfully!
    echo Anyone can now double-click EventLoop.jar or run: java -jar EventLoop.jar
) else (
    echo [ERROR] Packaging failed.
)

endlocal
