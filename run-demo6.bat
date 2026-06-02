@echo off
echo =======================================================
echo FastANSI Demo 6: Image Viewer
echo =======================================================
set IMAGE=%1
set MODE=%2
if "%IMAGE%"=="" set IMAGE=docs/screenshot.png
if "%MODE%"=="" set MODE=--block
call mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="fastansi.demos.Demo6ImageViewer" -Dexec.args="%IMAGE% %MODE%" -q
pause
