#!/bin/sh

source `pwd`/env.sh

DEPLOY_TO=server
DATE_OF_EXECUTION=`date`

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

activate_user_data ()
{
    # make backup of original file
    cp $USER_HOME/servers/server/env.sh $USER_HOME/servers/server/environment/env.sh.backup

    echo "# User-Data: START ($DATE_OF_EXECUTION)" >> $USER_HOME/servers/$DEPLOY_TO/env.sh
    echo "INSTANCE_NAME=`ec2-metadata -i | cut -f2 -d \" \"`" >> $USER_HOME/servers/$DEPLOY_TO/env.sh
    echo "INSTANCE_IP4=`ec2-metadata -v | cut -f2 -d \" \"`" >> $USER_HOME/servers/$DEPLOY_TO/env.sh
    echo "INSTANCE_DNS=`ec2-metadata -p | cut -f2 -d \" \"`" >> $USER_HOME/servers/$DEPLOY_TO/env.sh
    echo "INSTANCE_ID=\"$INSTANCE_NAME ($INSTANCE_IP4)\"" >> $USER_HOME/servers/$DEPLOY_TO/env.sh

    VARS=$(ec2-metadata -d | sed "s/user-data\: //g")
    for var in $VARS; do
        echo $var >> $USER_HOME/servers/$DEPLOY_TO/env.sh
    done
    echo "# User-Data: END" >> $USER_HOME/servers/$DEPLOY_TO/env.sh
    
    # make sure to reload data
    source `pwd`/env.sh
}

install_environment ()
{
    if [[ $USE_ENVIRONMENT != "" ]]; then
        # clean up directory to really make sure that there are no files left
        rm -rf $USER_HOME/servers/server/environment
        mkdir $USER_HOME/servers/server/environment
        echo "Using environment http://releases.sapsailing.com/environments/$USE_ENVIRONMENT"
        wget -P environment http://releases.sapsailing.com/environments/$USE_ENVIRONMENT
        echo "# Environment: START ($DATE_OF_EXECUTION)" >> $USER_HOME/servers/$DEPLOY_TO/env.sh
        cat $USER_HOME/servers/server/environment/$USE_ENVIRONMENT >> $USER_HOME/servers/server/env.sh
        echo "# Environment: END" >> $USER_HOME/servers/$DEPLOY_TO/env.sh

        # make sure to reload data
        source `pwd`/env.sh
    fi
}

load_from_release_file ()
{
    cd $USER_HOME/servers/$DEPLOY_TO
    rm -f $USER_HOME/servers/server/$INSTALL_FROM_RELEASE.tar.gz*
    rm -rf plugins start stop status native-libraries org.eclipse.osgi *.tar.gz
    echo "Loading from release file http://releases.sapsailing.com/$INSTALL_FROM_RELEASE/$INSTALL_FROM_RELEASE.tar.gz"
    wget http://releases.sapsailing.com/$INSTALL_FROM_RELEASE/$INSTALL_FROM_RELEASE.tar.gz
    mv env.sh env.sh.preserved
    tar xvzf $INSTALL_FROM_RELEASE.tar.gz
    mv env.sh.preserved env.sh
    echo "Configuration for this server is unchanged - just binaries have been changed."
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

OPERATION=$1
PARAM=$2

if [[ ! -z "$ON_AMAZON" ]]; then
    checks

    if [[ $OPERATION == "auto-install" ]]; then
        # first check and activate everything found in user data
        # then download and install environment
        activate_user_data
        install_environment

        if [[ $INSTALL_FROM_RELEASE != "" ]]; then
            load_from_release_file
        else
            checkout_code
            build
            deploy
        fi

    elif [[ $OPERATION == "install-release" ]]; then
        INSTALL_FROM_RELEASE=$PARAM
        if [[ $INSTALL_FROM_RELEASE == "" ]]; then
            echo "You need to provide the name of a release from http://releases.sapsailing.com/"
            exit 1
        fi
        load_from_release_file

    elif [[ $OPERATION == "update-environment" ]]; then
        USE_ENVIRONMENT=$PARAM
        if [[ $USE_ENVIRONMENT == "" ]]; then
            echo "You need to provide the name of an environment from http://releases.sapsailing.com/environments"
            exit 1
        fi
        install_environment

        echo "Configuration for this server is now:"
        echo ""
        echo "SERVER_NAME: $SERVER_NAME"
        echo "MEMORY: $MEMORY"
        echo "SERVER_PORT: $SERVER_PORT"
        echo "TELNET_PORT: $TELNET_PORT"
        echo "MONGODB_HOST: $MONGODB_HOST"
        echo "MONGODB_PORT: $MONGODB_PORT"
        echo "EXPEDITION_PORT: $EXPEDITION_PORT"
        echo "REPLICATION_HOST: $REPLICATION_HOST"
        echo "REPLICATION_CHANNEL: $REPLICATION_CHANNEL"
        echo "ADDITIONAL_ARGS: $ADDITIONAL_JAVA_ARGS"
        echo ""
        echo "INSTALL_FROM_RELEASE: $INSTALL_FROM_RELEASE"
        echo "DEPLOY_TO: $DEPLOY_TO"
        echo "BUILD_BEFORE_START: $BUILD_BEFORE_START"
        echo "USE_ENVRIONMENT: $USE_ENVIRONMENT"
        echo ""
        echo "JAVA_HOME: $JAVA_HOME"
        echo "INSTANCE_ID: $INSTANCE_ID"
        echo ""
    else
        echo "Script to prepare a Java instance running on Amazon."
        echo ""
        echo "install-release <release>: Downloads the release specified by the second option and overwrites all code for this server. Preserves env.sh."
        echo "update-env <environment>: Downloads and updates the environment with the one specified as a second option."
        exit 0
    fi

else
    echo "This server does not seem to be running on Amazon!"
fi
