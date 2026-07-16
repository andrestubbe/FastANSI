@echo off
chcp 65001 >nul
cls

echo ⚡ Building Main Project...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b %ERRORLEVEL% )

echo 🛠 Compiling Benchmark...
call mvn test-compile -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Compile failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Benchmark...
call mvn exec:java -Dexec.classpathScope=test -Dexec.mainClass="fastansi.FastANSIBenchmark" -q 2>nul

pause
