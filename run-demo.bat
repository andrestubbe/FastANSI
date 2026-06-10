@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo =========================================================
echo  FastANSI Demo  ^|  image ^& video player
echo =========================================================
:: Force Windows Console to support 24-bit True Color
reg add HKCU\Console /v VirtualTerminalLevel /t REG_DWORD /d 1 /f >nul 2>&1

:: Compile via maven first
call mvn test-compile dependency:copy-dependencies -DincludeScope=test -q

:: Run directly using java -cp to avoid Maven/Jansi stripping True Color
set ARGS=%*
if "%ARGS%"=="" set ARGS=docs\video.mp4 --loop

java -cp "target/test-classes;target/classes;target/dependency/*" fastansi.demos.Demo %ARGS%
