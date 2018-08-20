#!/bin/bash

# This file contains the configuration for the Java server

# *******************************************************
# ATTENTION: Make sure to always also check the variables at the end
# of this file as there could be overwritten ones!
# *******************************************************

SERVER_NAME=MASTER

# This is a default heap size only; the boot script of an instance (see
# configuration/sailing) will add a MEMORY assignment to this file in the
# server's directory that has a default value computed from the total
# memory installed in the machine and the number of server instances to
# start on that machine. This default can be overwritten by manually appending
# another MEMORY assignment at the end of the file or by defining an environment
# file with a MEMORY assignment which is then used in conjunction with refreshInstance.sh
# or by setting the MEMORY variable in the EC2 Instance Details section which will be appended
# at the end of the file.
MEMORY="6000m"

# Message Queue hostname where to
# send messages for replicas (this server is master)
REPLICATION_HOST=localhost
# For the port, use 0 for the RabbitMQ default or a specific port that your RabbitMQ server is listening on
REPLICATION_PORT=0
# The name of the message queuing fan-out exchange that this server will use in its role as replication master.
# Make sure this is unique so that no other master is writing to this exchange at any time.
REPLICATION_CHANNEL=sapsailinganalytics-master

TELNET_PORT=14888
SERVER_PORT=8888
MONGODB_HOST=localhost
MONGODB_PORT=27017
MONGODB_NAME=winddb
EXPEDITION_PORT=2010

# To start replication upon startup provide the fully-qualified names of the Replicable service classes
# for which to trigger replication. If you activate this make sure to
# set the REPLICATE_MASTER_EXCHANGE_NAME variable to the
# same channel the master is using in its REPLICATION_CHANNEL variable

# REPLICATE_ON_START=com.sap.sailing.server.impl.RacingEventServiceImpl,com.sap.sse.security.impl.SecurityServiceImpl,com.sap.sse.filestorage.impl.FileStorageManagementServiceImpl,com.sap.sse.mail.impl.MailServiceImpl,com.sap.sailing.polars.impl.PolarDataServiceImpl,com.sap.sailing.domain.racelogtracking.impl.fixtracker.RegattaLogFixTrackerRegattaListener
# Host where the master Java instance is running
# Make sure firewall configurations allow access
REPLICATE_MASTER_SERVLET_HOST=
REPLICATE_MASTER_SERVLET_PORT=

# Host where RabbitMQ is running 
REPLICATE_MASTER_QUEUE_HOST=
# Port that RabbitMQ is listening on (normally something like 5672); use 0 to connect to RabbitMQ's default port
REPLICATE_MASTER_QUEUE_PORT=0

# Exchange name that the master from which to auto-replicate is using as
# its REPLICATION_CHANNEL variable, mapping to the master's replication.exchangeName
# system property.
REPLICATE_MASTER_EXCHANGE_NAME=

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
ADDITIONAL_JAVA_ARGS="-Dpersistentcompetitors.clear=false -XX:ThreadPriorityPolicy=2 -XX:+UseG1GC -verbose:gc -XX:MaxGCPauseMillis=500 -XX:+PrintAdaptiveSizePolicy -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:logs/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M -XX:MaxGCPauseMillis=500 -XX:+PrintAdaptiveSizePolicy"

# Uncomment for use with SAP JVM only:
#ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -XX:+GCHistory -XX:GCHistoryFilename=logs/sapjvm_gc@PID.prf"

if [[ ! -d $JAVA_HOME ]] && [[ -f "/usr/libexec/java_home" ]]; then
    JAVA_HOME=`/usr/libexec/java_home`
fi

ON_AMAZON=`command -v ec2-metadata`

# **** Overwritten environment variables ****

