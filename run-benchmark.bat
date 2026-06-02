@echo off
call mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="fastansi.FastANSIBenchmark" -q
pause
