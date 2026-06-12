import os

files = {
'run-benchmark.bat': r'''@echo off
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
''',
'run-converter.bat': r'''@echo off
chcp 65001 >nul
cls

echo ⚡ Building Main Project...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b %ERRORLEVEL% )

echo 🛠 Compiling Converter...
call mvn test-compile dependency:copy-dependencies -DincludeScope=runtime -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Compile failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Converter...
:: Force Windows Console to support 24-bit True Color
reg add HKCU\Console /v VirtualTerminalLevel /t REG_DWORD /d 1 /f >nul 2>&1
java -cp "target/test-classes;target/classes;target/dependency/*" fastansi.cli.Converter %*

pause
''',
'run-demo.bat': r'''@echo off
chcp 65001 >nul
cls

echo ⚡ Building Main Project...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b %ERRORLEVEL% )

echo 🛠 Compiling Demo...
call mvn test-compile dependency:copy-dependencies -DincludeScope=test -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Compile failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Demo...
:: Force Windows Console to support 24-bit True Color
reg add HKCU\Console /v VirtualTerminalLevel /t REG_DWORD /d 1 /f >nul 2>&1
set ARGS=%*
if "%ARGS%"=="" set ARGS=docs\video.mp4 --loop
java -cp "target/test-classes;target/classes;target/dependency/*" fastansi.demos.Demo %ARGS%

pause
''',
'run-raymarch.bat': r'''@echo off
chcp 65001 >nul
cls

echo ⚡ Building Main Project...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b %ERRORLEVEL% )

echo 🛠 Compiling Raymarch Demo...
call mvn test-compile dependency:copy-dependencies -DincludeScope=test -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Compile failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running Raymarch Demo...
java -Dfile.encoding=UTF-8 -cp "target/test-classes;target/classes;target/dependency/*" fastansi.demos.RaymarchDemo %*

pause
'''
}

for filename, content in files.items():
    with open(filename, 'w', encoding='utf-8', newline='\r\n') as f:
        f.write(content)
