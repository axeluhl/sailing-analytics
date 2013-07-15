#!/bin/sh

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

ADDITIONAL_JAVA_ARGS="-XX:+UseMembar"

JAVA_HOME=$HOME/jdk1.7.0_02

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
fi

