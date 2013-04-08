#!/bin/bash
# see http://wiki.eclipse.org/Equinox/p2/Publisher for documentation

set -u

if [ "$ECLIPSE_HOME" = "" ]; then
    ECLIPSE_BIN=`which eclipse`
    ECLIPSE_HOME="`dirname $ECLIPSE_BIN`"
fi

SAILING_GIT_HOME="`pwd`/../../.."

TARGET_PLATFORM_BASE_DIR=${SAILING_GIT_HOME}/java/com.sap.sailing.targetplatform.base
ECLIPSE_VM_ARGS="-Xmx256m"
ECLIPSE_LAUNCHER=${ECLIPSE_HOME}/plugins/org.eclipse.equinox.launcher_*.jar

echo "Deploying metadata to $TARGET_PLATFORM_BASE_DIR/gen/p2..."

java -jar ${ECLIPSE_LAUNCHER} -consolelog -nosplash -verbose -application org.eclipse.equinox.p2.publisher.UpdateSitePublisher \
  -metadataRepository file:/${TARGET_PLATFORM_BASE_DIR}/gen/p2 \
  -artifactRepository file:/${TARGET_PLATFORM_BASE_DIR}/gen/p2 \
  -source ${TARGET_PLATFORM_BASE_DIR} \
  -publishArtifacts -publishArtifactRepository -compress -noDefaultIUs -vmargs ${ECLIPSE_VM_ARGS}
