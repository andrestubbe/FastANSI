@echo off
echo =======================================================
echo FastANSI Demo 5: Mutator
echo =======================================================
call mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="fastansi.demos.Demo5Mutator" -q
pause
