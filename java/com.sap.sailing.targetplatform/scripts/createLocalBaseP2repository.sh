#!/bin/bash
set -u
if [ "$ECLIPSE_HOME" = "" ]; then
    ECLIPSE_HOME=C:/Users/D056866/Sailing/eclipse
fi
if [ "$SAILING_GIT_HOME" = "" ]; then
    SAILING_GIT_HOME=C:/Users/D056866/Sailing/git
fi
TARGET_PLATFORM_BASE_DIR=${SAILING_GIT_HOME}/java/com.sap.sailing.targetplatform.base

ECLIPSE_VM_ARGS="-Xmx256m"
ECLIPSE_LAUNCHER=${ECLIPSE_HOME}/plugins/org.eclipse.equinox.launcher_*.jar

echo TARGET_PLATFORM_BASE_DIR is $SAILING_GIT_HOME
echo ECLIPSE_HOME is $ECLIPSE_HOME
echo Eclipse Launcher $ECLIPSE_LAUNCHER

java -jar ${ECLIPSE_LAUNCHER} -consolelog -nosplash -verbose -application org.eclipse.equinox.p2.publisher.UpdateSitePublisher \
  -metadataRepository file:/${TARGET_PLATFORM_BASE_DIR}/gen/p2 \
  -artifactRepository file:/${TARGET_PLATFORM_BASE_DIR}/gen/p2 \
  -source ${TARGET_PLATFORM_BASE_DIR} \
  -publishArtifacts -publishArtifactRepository -compress -noDefaultIUs -vmargs ${ECLIPSE_VM_ARGS}
