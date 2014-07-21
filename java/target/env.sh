#!/bin/bash

# This file contains the configuration for the Java server

# *******************************************************
# ATTENTION: Make sure to always also check the variables at the end
# of this file as there could be overwritten ones!
# *******************************************************

SERVER_NAME=MASTER

MEMORY="1024m"

# Queue Host and Name of the queue where to
# send messages for replicas (this server is master)
REPLICATION_HOST=localhost
REPLICATION_CHANNEL=sapsailinganalytics-master

TELNET_PORT=14888
SERVER_PORT=8888
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_NAME=winddb
EXPEDITION_PORT=2010

# Start replication upon startup?
# If you activate this make sure to
# set the REPLICATION_CHANNEL to the
# same channel the master is using
REPLICATE_ON_START=False

# Host where the master Java instance is running
# Make sure firewall configurations allow access
REPLICATE_MASTER_SERVLET_HOST=
REPLICATE_MASTER_SERVLET_PORT=

# Host and port where RabbitMQ is running 
# (port is normally something like 5672)
REPLICATE_MASTER_QUEUE_HOST=
REPLICATE_MASTER_QUEUE_PORT=

# Automatic build and test configuration
DEPLOY_TO=server
BUILD_BEFORE_START=False
BUILD_FROM=master
COMPILE_GWT=True
RUN_TESTS=False
CODE_DIRECTORY=code

# Specify an email adress that should be notified
# whenever a build or install has been completed
BUILD_COMPLETE_NOTIFY=

# Specify an email address that should be notified
# whenever the server has been started
SERVER_STARTUP_NOTIFY=

# Specify filename that is usually located at
# http://release.sapsailing.com/ that should
# be used as a base for the server
INSTALL_FROM_RELEASE=

# Specify name of file that can usually be found
# at http://release.sapsailing.com/environments
USE_ENVIRONMENT=

INSTANCE_ID="$SERVER_NAME:$SERVER_PORT"
ADDITIONAL_JAVA_ARGS="-Dpersistentcompetitors.clear=false -XX:+UseMembar -XX:+UseG1GC -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:logs/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"

JAVA_HOME=/opt/jdk1.7.0_60
if [[ ! -d $JAVA_HOME ]] && [[ -f "/usr/libexec/java_home" ]]; then
    JAVA_HOME=`/usr/libexec/java_home`
fi

ON_AMAZON=`command -v ec2-metadata`

# **** Overwritten environment variables ****

