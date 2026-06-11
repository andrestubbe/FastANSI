@echo off
chcp 65001 >nul
cd /d "%~dp0"

:: Compile via maven first
call mvn test-compile dependency:copy-dependencies -DincludeScope=test -q

:: Run the 3-Point Multi-Light Demo
java -Dfile.encoding=UTF-8 -cp "target/test-classes;target/classes;target/dependency/*" fastansi.demos.MultiLightDemo %*
