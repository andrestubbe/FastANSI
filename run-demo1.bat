@echo off
echo =======================================================
echo FastANSI Demo 1: Matrix Inspector
echo =======================================================
call mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="fastansi.demos.Demo1MatrixInspector" -q
pause
