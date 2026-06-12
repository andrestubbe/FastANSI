@echo off
chcp 65001 >nul
cls

echo ⚡ Building Main Project...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b %ERRORLEVEL% )

echo 🛠 Compiling Demo...
call mvn test-compile dependency:copy-dependencies -DincludeScope=test -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Compile failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Demo...
:: Force Windows Console to support 24-bit True Color
reg add HKCU\Console /v VirtualTerminalLevel /t REG_DWORD /d 1 /f >nul 2>&1
set ARGS=%*
if "%ARGS%"=="" set ARGS=docs\video.mp4 --loop
java -cp "target/test-classes;target/classes;target/dependency/*" fastansi.demos.Demo %ARGS%

pause
