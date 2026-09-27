@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo        EVENTLOOP - COMPILING APPLICATION
echo ========================================================

:: Check for JDK path
set JAVAC_CMD=javac
where javac >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Users\lenovo1\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\javac.exe" (
        set "JAVAC_CMD=C:\Users\lenovo1\.antigravity-ide\extensions\redhat.java-1.56.0-win32-x64\jre\21.0.12.1-win32-x86_64\bin\javac.exe"
    ) else (
        echo [ERROR] JDK 'javac' not found on PATH or default IDE location.
        echo Please ensure Java 11+ JDK is installed and added to PATH.
        exit /b 1
    )
)

if not exist bin mkdir bin

echo Generating source file manifest (with quotes for space safety)...
powershell -NoProfile -Command "Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { '\"' + $_.FullName.Replace('\', '/') + '\"' } | Set-Content -Encoding ASCII sources.txt"

echo Compiling Java source files...
"%JAVAC_CMD%" -cp "lib\*" -d bin @sources.txt
del sources.txt 2>nul

if %errorlevel% equ 0 (
    echo [SUCCESS] Compilation finished successfully. Output in bin\ directory.
) else (
    echo [ERROR] Compilation failed with error code %errorlevel%.
    exit /b %errorlevel%
)

endlocal
