#!/bin/sh

# This file contains the configuration for the Java server
# Make sure to always also check the variables at the end
# of this file as there could be overwritten ones.

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
EXPEDITION_PORT=2010

# Start replication upon startup
REPLICATE_ON_START=False
REPLICATE_MASTER_SERVLET_HOST=
REPLICATE_MASTER_SERVLET_PORT=
REPLICATE_MASTER_QUEUE_HOST=
REPLICATE_MASTER_QUEUE_PORT=

# Automatic build and test configuration
DEPLOY_TO=
BUILD_BEFORE_START=False
BUILD_FROM=master
COMPILE_GWT=True
RUN_TESTS=False
CODE_DIRECTORY=code
BUILD_COMPLETE_NOTIFY=simon.marcel.pamies@sap.com

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
ADDITIONAL_JAVA_ARGS="-XX:+UseMembar"

JAVA_HOME=$HOME/jdk1.7.0_02
if [[ ! -d $JAVA_HOME ]] && [[ -f "/usr/libexec/java_home" ]]; then
    JAVA_HOME=`/usr/libexec/java_home`
fi

ON_AMAZON=`command -v ec2-metadata`

# **** Overwritten environment variables ****

