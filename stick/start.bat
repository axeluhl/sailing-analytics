echo %CD%
call %CD%\\env.bat

cd %CD%server

set JAVA_BINARY=%JAVA_HOME%\bin\java.exe

set SED_ARGS="-i"

set scriptdir=%CD%
set LIB_DIR=%CD%\native-libraries

echo "Script directory is %scriptdir% and LIB_DIR is %LIB_DIR%"

REM Make really sure that parameters from config.ini are not used
%CD%\..\scripts\sed.exe %SED_ARGS% "/mongo.host/d" %CD%\configuration\config.ini
%CD%\..\scripts\sed.exe %SED_ARGS% "/mongo.port/d" %CD%\configuration\config.ini
%CD%\..\scripts\sed.exe %SED_ARGS% "/expedition.udp.port/d" %CD%\configuration\config.ini
%CD%\..\scripts\sed.exe %SED_ARGS% "/replication.exchangeName/d" %CD%\configuration\config.ini
%CD%\..\scripts\sed.exe %SED_ARGS% "/replication.exchangeHost/d" %CD%\configuration\config.ini
echo "Loaded MongoDB parameters"

REM Ensure that Jetty Port is set correctly
%CD%\..\scripts\sed.exe %SED_ARGS% "s/^.*jetty.port.*$/<Set name=\"port\"><Property name=\"jetty.port\" default=\"%SERVER_PORT%\"\\><\\Set>/g" %CD%\configuration\jetty\etc\jetty-selector.xml

REM Update monitoring with the right ports
%CD%\..\scripts\sed.exe %SED_ARGS% "s/127.0.0.1:[0-9][0-9][0-9][0-9]\\/127.0.0.1:%SERVER_PORT%\\/g" %CD%\configuration\monitoring.properties

REM Inject information for system
echo "Windows" > %CD%\configuration\jetty\version.txt

REM Apply app parameters from env.sh
set APP_PARAMETERS=-Dmongo.host=%MONGODB_HOST% -Dmongo.port=%MONGODB_PORT% -Dmongo.dbName=%MONGODB_NAME% -Dexpedition.udp.port=%EXPEDITION_PORT% -Dreplication.exchangeHost=%REPLICATION_HOST% -Dreplication.exchangeName=%REPLICATION_CHANNEL%

REM Apply parameters for automatic replication start
set REPLICATION_PARAMETERS=-Dreplicate.on.start=%REPLICATE_ON_START% -Dreplicate.master.servlet.host=%REPLICATE_MASTER_SERVLET_HOST% -Dreplicate.master.servlet.port=%REPLICATE_MASTER_SERVLET_PORT% -Dreplicate.master.queue.host=%REPLICATE_MASTER_QUEUE_HOST% -Dreplicate.master.queue.port=%REPLICATE_MASTER_QUEUE_PORT%

echo "Starting server..."
echo "SERVER_NAME: %SERVER_NAME%"
echo "SERVER_PORT: %SERVER_PORT%"
echo "MEMORY: %MEMORY%"
echo "TELNET_PORT: %TELNET_PORT%"
echo "MONGODB_HOST: %MONGODB_HOST%"
echo "MONGODB_PORT: %MONGODB_PORT%"
echo "MONGODB_NAME: %MONGODB_NAME%"
echo "EXPEDITION_PORT: %EXPEDITION_PORT%"
echo "REPLICATION_CHANNEL: %REPLICATION_CHANNEL%"
echo %JAVA_BINARY% -D%SERVER_NAME% %ADDITIONAL_JAVA_ARGS% -Dcom.sap.sailing.server.name=%SERVER_NAME% %APP_PARAMETERS% %REPLICATION_PARAMETERS% -Djava.io.tmpdir=%CD%/tmp -Dfile.encoding=cp1252 -Djetty.home=%scriptdir%/configuration/jetty -Djava.util.logging.config.file=%scriptdir%/configuration/logging.properties -Djava.library.path=%LIB_DIR% -Dosgi.shell.telnet.port=%TELNET_PORT% -Xmx%MEMORY% -jar plugins/org.eclipse.equinox.launcher_1.3.0.v20130327-1440.jar -configuration %CD% -clean
%JAVA_BINARY% -D%SERVER_NAME% %ADDITIONAL_JAVA_ARGS% -Dcom.sap.sailing.server.name=%SERVER_NAME% %APP_PARAMETERS% %REPLICATION_PARAMETERS% -Djava.io.tmpdir=%CD%/tmp -Dfile.encoding=cp1252 -Djetty.home=%scriptdir%/configuration/jetty -Djava.util.logging.config.file=%scriptdir%/configuration/logging.properties -Djava.library.path=%LIB_DIR% -Dosgi.shell.telnet.port=%TELNET_PORT% -Xmx%MEMORY% -jar plugins/org.eclipse.equinox.launcher_1.3.0.v20130327-1440.jar -configuration %CD% -clean

echo "Started %SERVER_NAME% - use telnet localhost %TELNET_PORT% to connect to the OSGi console. Logs should be found in logs\sailing0.log.0"
