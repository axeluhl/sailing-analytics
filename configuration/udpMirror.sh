#!/bin/bash
export JAVA_HOME=/home/trac/jdk1.7.0_02
jar=`find plugins -name 'com.sap.sailing.expeditionconnector_*.jar' | sort | tail -1`
echo Using JAR file $jar

if [ $# -eq 0 ]; then
    ARGS="-v 2012 localhost 2010 localhost 2011 localhost 2013 localhost 2014"
else
    ARGS=$*
fi

echo "Using $ARGS"
java -cp "$jar" -Djava.util.logging.config.file=udpmirrorLog.properties com.sap.sailing.expeditionconnector.UDPMirror $ARGS
