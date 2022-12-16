#!/bin/bash

# This file contains the configuration for the Java server

# *******************************************************
# ATTENTION: Make sure to always also check the variables at the end
# of this file as there could be overwritten ones!
# *******************************************************

# Set the heap size here if you want to override the default which
# will compute a MEMORY assignment from the total
# memory installed in the machine and the number of server instances to
# start on that machine.
MEMORY="20000m"

# Host / port where the master Java instance is running
# Make sure firewall configurations allow access
#
# REPLICATE_MASTER_SERVLET_HOST=
# REPLICATE_MASTER_SERVLET_PORT=

# Exchange name that the master from which to auto-replicate is using as
# its REPLICATION_CHANNEL variable, mapping to the master's replication.exchangeName
# system property.
#
# REPLICATE_MASTER_EXCHANGE_NAME=

# Credentials for replication access to the master server
# Make sure, the user is granted the permission SERVER:REPLICATE:<server-name>
# Credentials can be provided either as a combination of username and password,
# or alternatively as a single bearer token that was obtained, e.g., through
#   curl -d "username=myuser&password=mysecretpassword" "https://master-server.sapsailing.com/security/api/restsecurity/access_token" | jq .access_token
# or by logging in to the master server using your web browser and then navigating to
#     https://master-server.sapsailing.com/security/api/restsecurity/access_token
# 
# REPLICATE_MASTER_USERNAME=
# REPLICATE_MASTER_PASSWORD=
# REPLICATE_MASTER_BEARER_TOKEN=

# Automatic build and test configuration
BUILD_BEFORE_START=False
BUILD_FROM=master
COMPILE_GWT=True
RUN_TESTS=False
CODE_DIRECTORY=code

# Specify an email adress that should be notified
# whenever a build or install has been completed
#
# BUILD_COMPLETE_NOTIFY=

# Specify an email address that should be notified
# whenever the server has been started
#
# SERVER_STARTUP_NOTIFY=

# Specify filename that is usually located at
# http://release.sapsailing.com/ that should
# be used as a base for the server
#
# INSTALL_FROM_RELEASE=

# Specify name of file that can usually be found
# at http://release.sapsailing.com/environments
#
# USE_ENVIRONMENT=

INSTANCE_ID="$SERVER_NAME:$SERVER_PORT"
if [[ ! -d $JAVA_HOME ]] && [[ -f "/usr/libexec/java_home" ]]; then
    JAVA_HOME=`/usr/libexec/java_home`
fi
JAVA_BINARY="$JAVA_HOME/bin/java"
JAVA_VERSION_OUTPUT=$("$JAVA_BINARY" -version 2>&1)
JAVA_VERSION=$(echo "$JAVA_VERSION_OUTPUT" | sed 's/^.* version "\(.*\)\.\(.*\)\..*".*$/\1.\2/; 1q')
export JAVA_11_LOGGING_ARGS="-Xlog:gc+ergo*=trace:file=logs/gc_ergo.log:time:filecount=10,filesize=100000 -Xlog:gc*:file=logs/gc.log:time:filecount=10,filesize=100000"
export JAVA_11_ARGS="-Dosgi.java.profile=file://`pwd`/JavaSE-11.profile --add-modules=ALL-SYSTEM -Djavax.xml.bind.JAXBContextFactory=com.sun.xml.bind.v2.ContextFactory -XX:ThreadPriorityPolicy=1 -XX:+UnlockExperimentalVMOptions -XX:+UseZGC ${JAVA_11_LOGGING_ARGS}"
export JAVA_8_LOGGING_ARGS="-XX:+PrintAdaptiveSizePolicy -XX:+PrintGCTimeStamps -XX:+PrintGCDetails -Xloggc:logs/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M"
echo JAVA_VERSION detected: $JAVA_VERSION
if echo $JAVA_VERSION | grep -q "^11\."; then
  echo Java 11 detected
  JAVA_VERSION_SPECIFIC_ARGS=$JAVA_11_ARGS
else
  echo Java other than 11 detected
  # options for use with SAP JVM only:
  if echo "$JAVA_VERSION_OUTPUT" | grep -q "SAP Java"; then
    ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -XX:+GCHistory -XX:GCHistoryFilename=logs/sapjvm_gc@PID.prf"
    BUILD=$( echo "$JAVA_VERSION_OUTPUT" | grep "(build [^ ]*)" )
    MAJOR=$( echo "$BUILD" | sed -e 's/^.*(build \([0-9]*\)\.\([0-9]*\)\.\([0-9]*\)).*$/\1/' )
    MINOR=$( echo "$BUILD" | sed -e 's/^.*(build \([0-9]*\)\.\([0-9]*\)\.\([0-9]*\)).*$/\2/' )
    UPDATE=$( echo "$BUILD" | sed -e 's/^.*(build \([0-9]*\)\.\([0-9]*\)\.\([0-9]*\)).*$/\3/' )
    echo "SAP JVM $MAJOR $MINOR $UPDATE detected"
    if [ $MAJOR -ge 8 -a $MINOR -ge 1 -a $UPDATE -ge 45 ]; then
      echo "Update 8.1.045 or later; using Java11 GC logging options"
      LOGGING_ARGS="$JAVA_11_LOGGING_ARGS"
    else 
      echo "Update before 8.1.045; using Java8 GC logging options"
      LOGGING_ARGS="$JAVA_8_LOGGING_ARGS"
    fi
  else
    # non-SAP JVM 8
    export LOGGING_ARGS="$JAVA_8_LOGGING_ARGS"
  fi
  export JAVA_8_ARGS="-XX:ThreadPriorityPolicy=2 -XX:+UseG1GC ${LOGGING_ARGS}"
  JAVA_VERSION_SPECIFIC_ARGS=$JAVA_8_ARGS
fi

# White labeling: use -Dcom.sap.sse.debranding=true to remove branding images and text
#ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Dcom.sap.sse.debranding=true"
# Anniversary calculation:
#ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -DAnniversaryRaceDeterminator.enabled=true"
ADDITIONAL_JAVA_ARGS="$JAVA_VERSION_SPECIFIC_ARGS $ADDITIONAL_JAVA_ARGS -Dpersistentcompetitors.clear=false -Drestore.tracked.races=true -XX:MaxGCPauseMillis=500 -Dorg.eclipse.jetty.LEVEL=OFF -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog -Djava.naming.factory.url.pkgs=org.eclipse.jetty.jndi -Djava.naming.factory.initial=org.eclipse.jetty.jndi.InitialContextFactory"
# Use the following to obtain initial polar sheet data from the archive server, without live replication:
#ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Dpolardata.source.url=https://www.sapsailing.com"
# Use the following to obtain initial models for wind estimation from maneuvers from the archive server, without live replication:
#ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Dwindestimation.source.url=https://www.sapsailing.com"

# Custom event management URL: use -Dcom.sap.sailing.eventmanagement.url to modify from hardcoded default (https://my.sapsailing.com) to, e.g., https://dev.sapsailing.com
#ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Dcom.sap.sailing.eventmanagement.url=https://dev.sapsailing.com"

# To enable the use of the shared SecurityService and SharedSailingData from security-service.sapsailing.com, uncomment and fill in the following:
#ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Dsecurity.sharedAcrossSubdomainsOf=sapsailing.com -Dsecurity.baseUrlForCrossDomainStorage=https://security-service.sapsailing.com -Dgwt.acceptableCrossDomainStorageRequestOriginRegexp=https?://(.*\.)?sapsailing\.com(:[0-9]*)?$"
#REPLICATE_ON_START=com.sap.sse.security.impl.SecurityServiceImpl,com.sap.sailing.shared.server.impl.SharedSailingDataImpl
#REPLICATE_MASTER_SERVLET_HOST=security-service.sapsailing.com
#REPLICATE_MASTER_SERVLET_PORT=443
#REPLICATE_MASTER_EXCHANGE_NAME=security_service
# Obtain the bearer token for user security-service-replicator by logging on to https://security-service.sapsailing.com and then
# getting https://security-service.sapsailing.com/security/api/restsecurity/access_token
#REPLICATE_MASTER_BEARER_TOKEN="..."

ON_AMAZON=`command -v ec2-metadata`
### End of Standard env.sh ###
# live-master-server
REPLICATION_HOST=rabbit.internal.sapsailing.com
TELNET_PORT=14888
SERVER_PORT=8888
MONGODB_HOST=dbserver.internal.sapsailing.com
MONGODB_PORT=10202
EXPEDITION_PORT=2010
REPLICATE_ON_START=
REPLICATE_MASTER_SERVLET_HOST=
REPLICATE_MASTER_SERVLET_PORT=
REPLICATE_MASTER_QUEUE_HOST=rabbit.internal.sapsailing.com
REPLICATE_MASTER_QUEUE_PORT=5672
REPLICATION_CHANNEL=SEASCAPE
ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Xms$MEMORY -Dorg.eclipse.jetty.LEVEL=OFF -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog -XX:ThreadPriorityPolicy=2"

# To enable the use of the shared SecurityService and SharedSailingData from security-service.sapsailing.com:
ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Dsecurity.sharedAcrossSubdomainsOf=sapsailing.com -Dsecurity.baseUrlForCrossDomainStorage=https://security-service.sapsailing.com -Dgwt.acceptableCrossDomainStorageRequestOriginRegexp=https?://(.*\.)?sapsailing\.com(:[0-9]*)?$"
REPLICATE_ON_START=com.sap.sse.security.impl.SecurityServiceImpl,com.sap.sailing.shared.server.impl.SharedSailingDataImpl,com.sap.sse.landscape.aws.impl.AwsLandscapeStateImpl
REPLICATE_MASTER_SERVLET_HOST=security-service.sapsailing.com
REPLICATE_MASTER_SERVLET_PORT=443
REPLICATE_MASTER_EXCHANGE_NAME=security_service

# Provide authentication credentials for a user on the security-service.sapsailing.com permitted to replicate, either by username/password...
#REPLICATE_MASTER_USERNAME=(user for replicator login on security-service.sapsailing.com server having SERVER:REPLICATE:&lt;server-name&gt; permission)
#REPLICATE_MASTER_PASSWORD=(password of the user for replication login on security-service.sapsailing.com)
# Or by bearer token, obtained, e.g., through
#   curl -d "username=myuser&password=mysecretpassword" "https://security-service.sapsailing.com/security/api/restsecurity/access_token" | jq .access_token
# or by logging in to the security-service.sapsailing.com server with a user having the SERVER:REPLICATE:security-service and SERVER:READ_REPLICATOR:security-service
# permissions, using your web browser and then navigating to
#     https://security-service.sapsailing.com/security/api/restsecurity/access_token
REPLICATE_MASTER_BEARER_TOKEN="Gecx+W/dwFKRAxFbIvC/IMafEnJ8kTQF+MlYNVhEwD4="

DEPLOY_TO=seascape
SERVER_NAME=SEASCAPE
MONGODB_URI="mongodb://mongo0.internal.sapsailing.com,mongo1.internal.sapsailing.com/seascape?replicaSet=live&retryWrites=true&readPreference=nearest"
EXPEDITION_PORT=2057
SERVER_PORT=8935
TELNET_PORT=14935

