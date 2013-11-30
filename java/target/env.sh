#!/bin/sh

SERVER_NAME=DEV

MEMORY="2048m"

REPLICATION_HOST=localhost
REPLICATION_CHANNEL=sapsailinganalytics-dev

TELNET_PORT=14886
SERVER_PORT=8886
MONGODB_HOST=localhost
MONGODB_PORT=10200
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
    echo "Setting JAVA_HOME to discovered $JAVA_HOME"
fi

# Make it possible to overwrite configuration
# by using user-data that is injected into an instance
ON_AMAZON=`command -v ec2-metadata`
if [[ ! -z "$ON_AMAZON" ]]; then
  echo "This server is running on Amazon. Following data is being used (based on user-data injected into instance):"
  VARS=$(ec2-metadata -d | sed "s/user-data\: //g")
  for var in $VARS; do
        echo $var
        export $var
  done
    
  # set directory where to deploy code to
  # this is fixed on EC2 instances
  DEPLOY_TO=server
  INSTANCE_NAME=`ec2-metadata -i | cut -f2 -d " "`
  INSTANCE_IP4=`ec2-metadata -v | cut -f2 -d " "`
  INSTANCE_DNS=`ec2-metadata -p | cut -f2 -d " "`
  INSTANCE_ID="$INSTANCE_NAME ($INSTANCE_IP4)"
fi


echo "SERVER_NAME: $SERVER_NAME"
echo "SERVER_PORT: $SERVER_PORT"
echo "MEMORY: $MEMORY"
echo "TELNET_PORT: $TELNET_PORT"
echo "MONGODB_HOST: $MONGODB_HOST"
echo "MONGODB_PORT: $MONGODB_PORT"
echo "EXPEDITION_PORT: $EXPEDITION_PORT"
echo "REPLICATION_CHANNEL: $REPLICATION_CHANNEL"
