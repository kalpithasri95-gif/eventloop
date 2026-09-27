@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo        EVENTLOOP - LAUNCHING DESKTOP GUI
echo ========================================================

:: Check for Java runtime
set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Users\lenovo1\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\java.exe" (
        set "JAVA_CMD=C:\Users\lenovo1\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\java.exe"
    ) else (
        echo [ERROR] Java runtime 'java' not found on PATH or default location.
        pause
        exit /b 1
    )
)

if not exist bin\com\eventloop\main\EventLoopApp.class (
    echo Binaries not found. Running compilation first...
    call compile.bat
)

echo Starting EventLoop Desktop Application...
"%JAVA_CMD%" -cp "bin;lib\*" com.eventloop.main.EventLoopApp

endlocal
