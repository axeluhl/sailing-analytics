#!/bin/bash
# Uses a gradle build in java/com.amazon.aws.aws-java-api to obtain the JARs for the AWS Java API (SDK)
# into the lib/ folder there, then generates a corresponding .classpath,  META-INF/MANIFEST.MF, and build.properties file.
# Then, the java/com.amazon.aws.aws-java-api.updatesite/features/aws-sdk/feature.xml is adjusted to reflect the current version.
# The update site is then built locally into java/com.amazon.aws.aws-java-api.updatesite/target/repository which can then
# be tested with the local target platform definition. If everything works fine, the uploadAwsApiRepositoryToServer.sh script
# can be used to update the repository contents at p2.sapsailing.com with the updated local target platform repository contents.
LIB=lib
CLASSPATH_FILE=".classpath"
MANIFEST_FILE="MANIFEST.MF"
BUILD_PROPERTIES_FILE="build.properties"
WORKSPACE=`realpath \`dirname $0\`/../..`
UPDATE_SITE_PROJECT=${WORKSPACE}/java/com.amazon.aws.aws-java-api.updatesite
FEATURE_XML=${UPDATE_SITE_PROJECT}/features/aws-sdk/feature.xml
TARGET_DEFINITION="${WORKSPACE}/java/com.sap.sailing.targetplatform/definitions/race-analysis-p2-remote.target"
WRAPPER_BUNDLE="${WORKSPACE}/java/com.amazon.aws.aws-java-api"
cd ${WRAPPER_BUNDLE}
echo "Creating .project file from dot_project template to allow for Eclipse workspace import..."
cp dot_project .project
echo "Downloading libraries..."
rm -rf ${LIB}/*
${WORKSPACE}/gradlew downloadLibs
cd ${LIB}
VERSION=`ls -1 aws-core-*.jar | grep -v -- -sources | sed -e 's/aws-core-\([.0-9]*\)\.jar/\1/'`
echo VERSION=${VERSION}
LIBS=`ls -1 | grep -v -- -sources\.jar`
echo "Generating the .classpath file..."
echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<classpath>" >${WRAPPER_BUNDLE}/${CLASSPATH_FILE}
for l in ${LIBS}; do
  SOURCES_JAR=`basename $l .jar`-sources.jar
  if [ -f ${SOURCES_JAR} ]; then
    SOURCEPATH=" sourcepath=\"${LIB}/${SOURCES_JAR}\""
  else
    SOURCEPATH=""
  fi
  echo "        <classpathentry exported=\"true\" kind=\"lib\" path=\"${LIB}/${l}\"${SOURCEPATH}/>" >>${WRAPPER_BUNDLE}/${CLASSPATH_FILE}
done
echo "        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8\"/>
        <classpathentry kind=\"con\" path=\"org.eclipse.pde.core.requiredPlugins\"/>
        <classpathentry kind=\"output\" path=\"bin\"/>
</classpath>" >>${WRAPPER_BUNDLE}/${CLASSPATH_FILE}
echo "Patching version ${VERSION} into pom.xml..."
# exclude SNAPSHOT version used for the parent pom; only match the explicit SDK version
sed -i -e 's/<version>\([0-9.]*\)<\/version>/<version>'${VERSION}'<\/version>/' ${WRAPPER_BUNDLE}/pom.xml
echo "Generating the META-INF/MANIFEST.MF file..."
echo -n "Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: aws-java-api
Bundle-SymbolicName: com.amazon.aws.aws-java-api
Bundle-Version: ${VERSION}
Bundle-Vendor: Amazon
Bundle-RequiredExecutionEnvironment: JavaSE-1.8
Bundle-ClassPath: " >${WRAPPER_BUNDLE}/META-INF/${MANIFEST_FILE}
for l in ${LIBS}; do
  echo -n "lib/${l},
 " >>${WRAPPER_BUNDLE}/META-INF/${MANIFEST_FILE}
done
echo "   ...determining exported packages from libs to generate Export-Package in manifest..."
echo -n ".
Automatic-Module-Name: com.amazon.aws.aws-java-api
Export-Package:" >>${WRAPPER_BUNDLE}/META-INF/${MANIFEST_FILE}
PACKAGES=$(for l in ${LIBS}; do
  jar tvf ${l} | grep "\.class\>" | sed -e 's/^.* \([^ ]*\)$/\1/' -e 's/\/[^/]*\.class\>//' | grep "^software/amazon"
done | sort -u | tr / . )
for p in `echo "${PACKAGES}" | while read i; do echo $i | sed -e 's/^\([-a-zA-Z0-9_.]*\)\>.*$/\1/'; done | head --lines=-1`; do
   echo " ${p}," >>${WRAPPER_BUNDLE}/META-INF/${MANIFEST_FILE}
done
for p in `echo "${PACKAGES}" | while read i; do echo $i | sed -e 's/^\([-a-zA-Z0-9_.]*\)\>.*$/\1/'; done | tail --lines=1`; do
   echo " ${p}" >>${WRAPPER_BUNDLE}/META-INF/${MANIFEST_FILE}
done
echo "Generating build.properties..."
echo -n "bin.includes = META-INF/,\\
               ." >${WRAPPER_BUNDLE}/${BUILD_PROPERTIES_FILE}
for l in ${LIBS}; do
  echo -n ",\\
               lib/${l}" >>${WRAPPER_BUNDLE}/${BUILD_PROPERTIES_FILE}
done
echo >>${WRAPPER_BUNDLE}/${BUILD_PROPERTIES_FILE}
echo "Building the wrapper bundle..."
cd ..
mvn clean install
mkdir -p ${UPDATE_SITE_PROJECT}/plugins/aws-sdk
rm -rf ${UPDATE_SITE_PROJECT}/plugins/aws-sdk/*
mv bin/com.amazon.aws.aws-java-api-${VERSION}.jar ${UPDATE_SITE_PROJECT}/plugins/aws-sdk
cd ${UPDATE_SITE_PROJECT}
echo "Patching update site's feature.xml..."
sed -i -e 's/^\( *\)version="[0-9.]*"/\1version="'${VERSION}'"/' ${FEATURE_XML}
echo "Building update site..."
mvn clean install
echo "Patching SDK version in target platform definition ${TARGET_DEFINITION}..."
sed -i -e 's/<unit id="com.amazon.aws.aws-java-api.feature.group" version="[0-9.]*"\/>/<unit id="com.amazon.aws.aws-java-api.feature.group" version="'${VERSION}'"\/>/' ${TARGET_DEFINITION}
echo "You may test your target platform locally by creating race-analysis-p2-local.target by running the script createLocalTargetDef.sh."
echo "You can also try a Hudson build with the -v option, generating and using the local target platform during the build."
echo "When all this works, update the P2 repository at p2.sapsailing.com using the script uploadAwsApiRepositoryToServer.sh."
