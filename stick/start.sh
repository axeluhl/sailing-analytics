#!/bin/sh

# source variables
. `pwd`/env.sh

# Start mongodb
echo "Starting mongodb... (can take up to three minutes)"
mongodb/$SYSTEM_TYPE/bin/mongod --fork -f `pwd`/mongodb.cfg
MONGO_PID=$!
echo $MONGO_PID > mongodb-data/mongo.pid
sleep 120s
echo "Started MongoDB"

cd server

PARAM=$@

# make some checks
JAVA_BINARY=$JAVA_HOME/bin/java
if [[ ! -d "$JAVA_HOME" ]]; then
    echo "Could not find $JAVA_BINARY set in env.sh. Trying to find the correct one..."
    JAVA_VERSION=$(java -version 2>&1 | sed 's/java version "\(.*\)\.\(.*\)\..*"/\1\2/; 1q')
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo "The current Java version ($JAVA_VERSION) does not match the requirements (>= 1.7)."
        exit
    fi
    JAVA_BINARY=`which java`
    echo "Using Java from $JAVA_BINARY"
fi

PLATFORM=`uname`
SED_ARGS="-i"
if [[ $PLATFORM == "Darwin" ]]; then
    SED_ARGS="-i \"\""
fi

scriptdir=`dirname "$0"`
LIB_DIR=`pwd`/native-libraries

if [[ ! -d `pwd`/logs ]]; then
    mkdir logs
fi

if [[ ! -d `pwd`/tmp ]]; then
    mkdir tmp
fi

echo "Script directory is $scriptdir and LIB_DIR is $LIB_DIR"

# Make really sure that parameters from config.ini are not used
sed $SED_ARGS "/mongo.host/d" ./configuration/config.ini
sed $SED_ARGS "/mongo.port/d" ./configuration/config.ini
sed $SED_ARGS "/expedition.udp.port/d" ./configuration/config.ini
sed $SED_ARGS "/replication.exchangeName/d" ./configuration/config.ini
sed $SED_ARGS "/replication.exchangeHost/d" ./configuration/config.ini

# Ensure that Jetty Port is set correctly
sed $SED_ARGS "s/^.*jetty.port.*$/<Set name=\"port\"><Property name=\"jetty.port\" default=\"$SERVER_PORT\"\/><\/Set>/g" ./configuration/jetty/etc/jetty-selector.xml

# Update monitoring with the right ports
sed $SED_ARGS "s/127.0.0.1:[0-9][0-9][0-9][0-9]\//127.0.0.1:$SERVER_PORT\//g" ./configuration/monitoring.properties

# Inject information for system
HEAD_DATE=$(date "+%Y%m%d%H%M")
sed $SED_ARGS "s/System:.*$/ System: $MONGODB_HOST:$MONGODB_PORT\/$MONGODB_NAME-$EXPEDITION_PORT-$REPLICATION_HOST\/$REPLICATION_CHANNEL Started: $HEAD_DATE/g" ./configuration/jetty/version.txt

# Apply app parameters from env.sh
APP_PARAMETERS="-Dmongo.host=$MONGODB_HOST -Dmongo.port=$MONGODB_PORT -Dmongo.dbName=$MONGODB_NAME -Dexpedition.udp.port=$EXPEDITION_PORT -Dreplication.exchangeHost=$REPLICATION_HOST -Dreplication.exchangeName=$REPLICATION_CHANNEL"

# Apply parameters for automatic replication start
REPLICATION_PARAMETERS="-Dreplicate.on.start=$REPLICATE_ON_START -Dreplicate.master.servlet.host=$REPLICATE_MASTER_SERVLET_HOST -Dreplicate.master.servlet.port=$REPLICATE_MASTER_SERVLET_PORT -Dreplicate.master.queue.host=$REPLICATE_MASTER_QUEUE_HOST -Dreplicate.master.queue.port=$REPLICATE_MASTER_QUEUE_PORT"

nohup $JAVA_BINARY -D$SERVER_NAME $ADDITIONAL_JAVA_ARGS -Dcom.sap.sailing.server.name=$SERVER_NAME $APP_PARAMETERS $REPLICATION_PARAMETERS -Djava.io.tmpdir=`pwd`/tmp -Dfile.encoding=cp1252 -Djetty.home=$scriptdir/configuration/jetty -Djava.util.logging.config.file=$scriptdir/configuration/logging.properties -Djava.library.path=$LIB_DIR -Dosgi.shell.telnet.port=$TELNET_PORT -Xmx$MEMORY -jar plugins/org.eclipse.equinox.launcher_1.3.0.v20130327-1440.jar -configuration ./ -clean 2>/dev/null >/dev/null &
JAVA_PID=$!
echo "Starting server... (PID: $JAVA_PID)"
echo "SERVER_NAME: $SERVER_NAME"
echo "SERVER_PORT: $SERVER_PORT"
echo "MEMORY: $MEMORY"
echo "TELNET_PORT: $TELNET_PORT"
echo "MONGODB_HOST: $MONGODB_HOST"
echo "MONGODB_PORT: $MONGODB_PORT"
echo "MONGODB_NAME: $MONGODB_NAME"
echo "EXPEDITION_PORT: $EXPEDITION_PORT"
echo "REPLICATION_CHANNEL: $REPLICATION_CHANNEL"

echo $JAVA_PID > server.pid
sleep 5s
echo "Started $SERVER_NAME - use telnet localhost $TELNET_PORT to connect to the OSGi console. Logs should be found in logs/sailing0.log.0"
tail -f logs/sailing0.log.0
