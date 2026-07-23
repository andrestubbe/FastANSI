@echo off
chcp 65001 >nul
call mvn test-compile -q
java -Dfile.encoding=UTF-8 -cp "target\classes;target\test-classes" fastansi.demos.HalfBlockImageDemo %*
