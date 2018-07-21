echo %CD%
call %CD%\\env.bat

REM Start mongodb
echo Starting mongodb... database should be ready after some minutes.
echo If you start the database for the first time wait at least 5 minutes
%CD%\mongodb\Windows\bin\mongod.exe -f %CD%\mongodb-windows.cfg
