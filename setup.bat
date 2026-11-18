@echo off
setlocal
cd /d "%~dp0"
if not exist lib mkdir lib
if not exist lib\mysql-connector-j-9.5.0.jar (
  echo Downloading MySQL Connector/J 9.5.0...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.5.0/mysql-connector-j-9.5.0.jar' -OutFile 'lib/mysql-connector-j-9.5.0.jar'"
  if errorlevel 1 (echo Download failed. Check your internet connection and try again.& pause & exit /b 1)
)
if not exist lib\mysql-connector-j-9.5.0.jar (echo Connector file was not created.& pause & exit /b 1)
echo Connector ready.
echo Next, import database\qwikbite.sql using the MySQL command line.
pause
