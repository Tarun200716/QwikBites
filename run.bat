@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (echo JDK not found. Install JDK 17 or newer and reopen this window.& pause & exit /b 1)
if not exist lib\mysql-connector-j-9.5.0.jar (echo Missing MySQL connector. Run setup.bat first.& pause & exit /b 1)
if not exist out mkdir out
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -cp "lib\mysql-connector-j-9.5.0.jar" -d out @sources.txt
if errorlevel 1 (echo Compilation failed. Fix the errors above before launching.& pause & exit /b 1)
java -cp "out;lib\mysql-connector-j-9.5.0.jar" com.qwikbite.Main
pause
