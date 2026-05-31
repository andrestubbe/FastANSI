@echo off
echo =======================================================
echo FastANSI Demo 2: ANSI Stripper
echo =======================================================
call mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="fastansi.demos.Demo2AnsiStripper" -q
pause
