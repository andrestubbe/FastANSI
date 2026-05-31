@echo off
echo =======================================================
echo FastANSI JMH Benchmark
echo =======================================================
echo.
echo Compiling and running JMH Benchmark...
echo This will take about 1-2 minutes to warm up and measure.
echo.

call mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="fastansi.FastANSIBenchmark" -q

echo.
pause
