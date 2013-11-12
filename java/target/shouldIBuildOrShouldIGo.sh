#!/bin/sh

. `pwd`/env.sh

checks ()
{
    USER_HOME=~
    START_DIR=`pwd`
    PROJECT_HOME=$(find_project_home $START_DIR)

    # needed for maven on sapsailing.com to work correctly
    if [ -f $USER_HOME/.bash_profile ]; then
        source $USER_HOME/.bash_profile
    fi

    JAVA_BINARY=$JAVA_HOME/bin/java
    if [[ ! -d "$JAVA_HOME" ]]; then
        echo "Could not find $JAVA_BINARY set in env.sh. Trying to find the correct one..."
        JAVA_VERSION=$(java -version 2>&1 | sed 's/java version "\(.*\)\.\(.*\)\..*"/\1\2/; 1q')
        if [ "$JAVA_VERSION" -lt 17 ]; then
            echo "The current Java version ($JAVA_VERSION) does not match the requirements (>= 1.7)."
            exit 10
        fi
        JAVA_BINARY=`which java`
        echo "Using Java from $JAVA_BINARY"
    fi
}

find_project_home () 
{
    if [[ $1 == '/' ]] || [[ $1 == "" ]]; then
        echo ""
        return 0
    fi

    if [ ! -d "$1/.git" ]; then
        PARENT_DIR=`cd $1/..;pwd`
        OUTPUT=$(find_project_home $PARENT_DIR)

        if [ "$OUTPUT" = "" ] && [ -d "$PARENT_DIR/$CODE_DIRECTORY" ] && [ -d "$PARENT_DIR/$CODE_DIRECTORY/.git" ]; then
            OUTPUT="$PARENT_DIR/$CODE_DIRECTORY"
        fi
        echo $OUTPUT
        return 0
    fi

    echo $1 | sed -e 's/\/cygdrive\/\([a-zA-Z]\)/\1:/'
}

load_from_release_file ()
{
    cd $USER_HOME/servers/server
    rm -rf plugins start stop status native-libraries org.eclipse.osgi
    echo "Loading from release file http://releases.sapsailing.com/$INSTALL_FROM_RELEASE/$INSTALL_FROM_RELEASE.tar.gz"
    `which wget` http://releases.sapsailing.com/$INSTALL_FROM_RELEASE/$INSTALL_FROM_RELEASE.tar.gz
    `which tar` xvzf $INSTALL_FROM_RELEASE.tar.gz
    echo "Using environment http://releases.sapsailing.com/environments/$USE_ENVIRONMENT"
    `which wget` http://releases.sapsailing.com/environments/$USE_ENVIRONMENT
    chmod a+x $USE_ENVIRONMENT
    ./$USE_ENVIRONMENT
    echo "Configuration for this server is:"
    echo "SERVER_NAME: $SERVER_NAME"
    echo "SERVER_PORT: $SERVER_PORT"
    echo "MEMORY: $MEMORY"
    echo "TELNET_PORT: $TELNET_PORT"
    echo "MONGODB_PORT: $MONGODB_PORT"
    echo "EXPEDITION_PORT: $EXPEDITION_PORT"
    echo "REPLICATION_CHANNEL: $REPLICATION_CHANNEL"
}

checkout_code ()
{
    cd $PROJECT_HOME
    GIT_BINARY=`which git`
    if [[ $COMPILE_GWT == "True" ]]; then
        # only reset if GWT gets compiled
        # if not p2build will not work
        $GIT_BINARY reset --hard
    fi
    $GIT_BINARY checkout $BUILD_FROM
    $GIT_BINARY pull
}

build ()
{
    cd $PROJECT_HOME
    TESTS="-t"
    if [[ $RUN_TESTS == "True" ]]; then
        TESTS=""
    fi
    GWT="-g"
    if [[ $COMPILE_GWT == "True" ]]; then
        GWT=""
    fi
    $PROJECT_HOME/configuration/buildAndUpdateProduct.sh $TESTS $GWT -u build
    STATUS=$?
    if [ $STATUS -eq 0 ]; then
        echo "Build Successful"
    else
        echo "Build Failed"
        exit 10
    fi 
}

deploy ()
{
    cd $PROJECT_HOME
    if [[ $DEPLOY_TO != "" ]]; then
        DEPLOY="-s $DEPLOY_TO"
    fi
    $PROJECT_HOME/configuration/buildAndUpdateProduct.sh -u $DEPLOY install
}

checks

if [[ $INSTALL_FROM_RELEASE != "" ]]; then
    load_from_release_file
else
    checkout_code
    build
    deploy
fi
