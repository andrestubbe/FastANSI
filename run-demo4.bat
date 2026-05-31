@echo off
echo =======================================================
echo FastANSI Demo 4: Realtime ANSI Interceptor
echo =======================================================
call mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="fastansi.demos.Demo4RealtimeInterceptor" -q
pause
