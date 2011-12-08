#!/bin/bash

set -u

ECLIPSE_VM_ARGS="-Xmx256m"
auncher_*.jar

java -jar ${ECLIPSE_EXE} -consolelog -nosplash -verbose -application org.eclipse.equinox.p2.publisher.UpdateSitePublisher \
  -metadataRepository file:/C:/Projects/sailing/dev/p2-repositories/sailing \
  -artifactRepository file:/C:/Projects/sailing/dev/p2-repositories/sailing \
  -source C:/Projects/sailing/dev/p2-repositories/sailing_libs_source \
  -publishArtifacts -publishArtifactRepository -compress -noDefaultIUs -vmargs ${ECLIPSE_VM_ARGS}
