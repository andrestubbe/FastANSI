@echo off
chcp 65001 >nul
call mvn test-compile -q
java -cp "target\classes;target\test-classes" fastansi.demos.Sixel1To1PixelDemo %*
