#!/bin/sh

if [[ "$JAVA_HOME" == "" ]]; then
    JAVA_HOME=$HOME/jdk1.7.0_02
    echo "Setting JAVA_HOME to $JAVA_HOME"
fi

TELNET_PORT=14885
SERVER_NAME=MASTER
