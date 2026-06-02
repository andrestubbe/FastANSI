@echo off
echo =======================================================
echo FastANSI Demo
echo =======================================================
set IMAGE=%1
set MODE=%2
if "%IMAGE%"=="" set IMAGE=docs/screenshot.png
if "%MODE%"=="" set MODE=--block
call mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="fastansi.demos.Demo" -Dexec.args="%IMAGE% %MODE%" -q
pause
