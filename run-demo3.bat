@echo off
echo =======================================================
echo FastANSI Demo 3: HTML Converter
echo =======================================================
call mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass="fastansi.demos.Demo3HtmlConverter" -q
pause
