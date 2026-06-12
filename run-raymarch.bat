@echo off
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
