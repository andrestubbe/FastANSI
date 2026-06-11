@echo off
:: Redirect stderr to nul to hide JDK 21+ sun.misc.Unsafe deprecation warnings from JMH
call mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="fastansi.FastANSIBenchmark" -q 2>nul
pause
