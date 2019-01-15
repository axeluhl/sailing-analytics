#!/bin/bash

source `pwd`/env.sh
DATE_OF_EXECUTION=`date`

# The following temporary file may be used by this script to dump EC2-provided user data
# variables to it; they can then be sourced from there and later appended to a new env.sh
ec2EnvVars_tmpFile=`mktemp /tmp/ec2EnvVars_XXX`

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

    # make sure to set email adresses
    if [[ $BUILD_COMPLETE_NOTIFY == "" ]]; then
        export BUILD_COMPLETE_NOTIFY=simon.marcel.pamies@sap.com
    fi

    if [[ $SERVER_STARTUP_NOTIFY == "" ]]; then
        export SERVER_STARTUP_NOTIFY=simon.marcel.pamies@sap.com
    fi

    if [[ $DEPLOY_TO == "" ]]; then
        DEPLOY_TO=server
    fi

    SERVER_HOME=$USER_HOME/servers/$DEPLOY_TO

    if [[ ! -d $SERVER_HOME ]]; then
        SERVER_HOME=`pwd`/../../servers/$DEPLOY_TO
        if [[ ! -d $SERVER_HOME ]]; then
            echo "Could not find the correct directory for the server - assumed $SERVER_HOME. Adapt DEPLOY_TO in env.sh to point to the right server directory."
            exit 10
        fi
    fi
}

copy_user_data_to_tmp_file ()
{
    echo "Reading user-data provided by Amazon instance data to $ec2EnvVars_tmpFile"

    VARS=$(ec2-metadata -d | sed "s/user-data\: //g")
    if [[ "$VARS" != "not available" ]]; then
        for var in $VARS; do
          echo $var >>"$ec2EnvVars_tmpFile"
        done
    else
        echo "No user data has been provided."
    fi
}

# loads the user data-provided variables by sourcing the script
activate_user_data ()
{
    # make sure to reload data
    source "$ec2EnvVars_tmpFile"
    INSTANCE_NAME=`ec2-metadata -i | cut -f2 -d " "`
    INSTANCE_IP4=`ec2-metadata -v | cut -f2 -d " "`
    INSTANCE_DNS=`ec2-metadata -p | cut -f2 -d " "`
    INSTANCE_ID="$INSTANCE_NAME ($INSTANCE_IP4)"
}

append_user_data_to_envsh ()
{
    # make backup of original file
    cp $SERVER_HOME/env.sh $SERVER_HOME/environment/env.sh.backup

    echo "# User-Data: START ($DATE_OF_EXECUTION)" >> $SERVER_HOME/env.sh
    echo "INSTANCE_NAME=`ec2-metadata -i | cut -f2 -d \" \"`" >> $SERVER_HOME/env.sh
    echo "INSTANCE_IP4=`ec2-metadata -v | cut -f2 -d \" \"`" >> $SERVER_HOME/env.sh
    echo "INSTANCE_INTERNAL_IP4=`ec2-metadata -o | cut -f2 -d \" \"`" >> $SERVER_HOME/env.sh
    echo "INSTANCE_DNS=`ec2-metadata -p | cut -f2 -d \" \"`" >> $SERVER_HOME/env.sh

    for var in `cat "$ec2EnvVars_tmpFile"`; do
        echo $var >> $SERVER_HOME/env.sh
        echo "Activated: $var"
    done

    echo "INSTANCE_ID=\"$INSTANCE_NAME ($INSTANCE_IP4)\"" >> $SERVER_HOME/env.sh
    echo "# User-Data: END" >> $SERVER_HOME/env.sh
    echo "Updated env.sh with data from user-data field!"
    echo ""
}

install_environment ()
{
    if [[ $USE_ENVIRONMENT != "" ]]; then
        # clean up directory to really make sure that there are no files left
        rm -rf $SERVER_HOME/environment
        mkdir $SERVER_HOME/environment
        echo "Using environment http://releases.sapsailing.com/environments/$USE_ENVIRONMENT"
        wget -P environment http://releases.sapsailing.com/environments/$USE_ENVIRONMENT
        echo "# Environment ($USE_ENVIRONMENT): START ($DATE_OF_EXECUTION)" >> $SERVER_HOME/env.sh
        cat $SERVER_HOME/environment/$USE_ENVIRONMENT >> $SERVER_HOME/env.sh
        echo "# Environment: END" >> $SERVER_HOME/env.sh

        echo "Updated env.sh with data from environment file!"
    else
        echo "No environment file specified!"
    fi
}

load_from_release_file ()
{
    if [[ $INSTALL_FROM_RELEASE != "" ]]; then
        echo "Build/Deployment process has been started - it can take 5 to 20 minutes until your instance is ready. " | mail -r simon.marcel.pamies@sap.com -s "Build or Deployment of $INSTANCE_ID to $SERVER_HOME for server $SERVER_NAME starting" $BUILD_COMPLETE_NOTIFY
        cd $SERVER_HOME
        rm -f $SERVER_HOME/$INSTALL_FROM_RELEASE.tar.gz*
        rm -rf plugins start stop status native-libraries org.eclipse.osgi *.tar.gz
        echo "Loading from release file http://releases.sapsailing.com/$INSTALL_FROM_RELEASE/$INSTALL_FROM_RELEASE.tar.gz"
        wget http://releases.sapsailing.com/$INSTALL_FROM_RELEASE/$INSTALL_FROM_RELEASE.tar.gz
        mv env.sh env.sh.preserved
	mv configuration/mail.properties configuration/mail.properties.preserved
	mv configuration/debug.properties configuration/debug.properties.preserved
        tar xvzf $INSTALL_FROM_RELEASE.tar.gz
        mv env.sh.preserved env.sh
	mv configuration/mail.properties.preserved configuration/mail.properties
	mv configuration/debug.properties.preserved configuration/debug.properties
        echo "Configuration for this server is unchanged - just binaries have been changed."
    else
        echo "The variable INSTALL_FROM_RELEASE has not been set therefore no release file will be installed!"
    fi
}

load_from_local_release_file ()
{
    if [[ $INSTALL_FROM_RELEASE != "" ]]; then
        cd $SERVER_HOME
        rm -rf plugins start stop status native-libraries org.eclipse.osgi
        echo "Loading from release file $INSTALL_FROM_RELEASE"
        mv env.sh env.sh.preserved
	mv configuration/mail.properties configuration/mail.properties.preserved
	mv configuration/debug.properties configuration/debug.properties.preserved
        tar xvzf $INSTALL_FROM_RELEASE
        mv env.sh.preserved env.sh
	mv configuration/mail.properties.preserved configuration/mail.properties
	mv configuration/debug.properties.preserved configuration/debug.properties
        echo "Configuration for this server is unchanged - just binaries have been changed."
    else
        echo "The variable INSTALL_FROM_RELEASE has not been set therefore no release file will be installed!"
    fi
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
    # check for available memory - build can not be started with less than 1GB
    MEM_TOTAL=`free -mt | grep Total | awk '{print $2}'`
    if [ $MEM_TOTAL -lt 924 ]; then
        echo "Could not start build process with less than 1GB of RAM!"
        echo "Not enough RAM for completing the build process! You need at least 1GB. Instance NOT started!" | mail -r simon.marcel.pamies@sap.com -s "Build of $INSTANCE_ID failed" $BUILD_COMPLETE_NOTIFY
    else
        if [[ $BUILD_BEFORE_START == "True" ]]; then
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
        else
            echo "The parameter BUILD_BEFORE_START is not set to True therefore no build will be executed!"
        fi
    fi
}

deploy ()
{
    cd $PROJECT_HOME
    if [[ $DEPLOY_TO != "" ]]; then
        DEPLOY="-s $DEPLOY_TO"
    fi

    $PROJECT_HOME/configuration/buildAndUpdateProduct.sh -u $DEPLOY install > $SERVER_HOME/last_automatic_build.txt
    STATUS=$?
    if [ $STATUS -eq 0 ]; then
        echo "Deployment Successful"
        echo "OK - check the attachment for more information." | mail -a $SERVER_HOME/last_automatic_build.txt -r simon.marcel.pamies@sap.com -s "Build or Deployment of $INSTANCE_ID complete" $BUILD_COMPLETE_NOTIFY
    else
        echo "Deployment Failed"
        echo "ERROR - check the attachment for more information." | mail -a $SERVER_HOME/last_automatic_build.txt -r simon.marcel.pamies@sap.com -s "Build of $INSTANCE_ID failed" $BUILD_COMPLETE_NOTIFY
    fi 
}

OPERATION=$1
PARAM=$2

checks
if [[ $OPERATION == "auto-install" ]]; then
    if [[ ! -z "$ON_AMAZON" ]]; then
        # first check and activate everything found in user data
	copy_user_data_to_tmp_file
        activate_user_data
        # then download and install environment and append to env.sh
        install_environment
	# finally, append user data to env.sh as it shall take precedence over the installed environment's defaults
	append_user_data_to_envsh

        # make sure to reload data
        source `pwd`/env.sh

        if [[ $INSTALL_FROM_RELEASE == "" ]] && [[ $BUILD_BEFORE_START != "True" ]]; then
            echo "I could not find any option telling me to download a release or to build! Possible cause: Your environment contains empty values for these variables!"
            exit 1
        fi

        echo ""
        echo "INSTALL_FROM_RELEASE: $INSTALL_FROM_RELEASE"
        echo "DEPLOY_TO: $DEPLOY_TO"
        echo "BUILD_BEFORE_START: $BUILD_BEFORE_START"
        echo "USE_ENVRIONMENT: $USE_ENVIRONMENT"
        echo ""

        if [[ $INSTALL_FROM_RELEASE != "" ]]; then
            load_from_release_file
        else
            checkout_code
            build
            deploy
        fi
    else
        echo "This server does not seem to be running on Amazon! Automatic install only works on Amazon instances."
        exit 1
    fi

elif [[ $OPERATION == "install-release" ]]; then
    INSTALL_FROM_RELEASE=$PARAM
    if [[ $INSTALL_FROM_RELEASE == "" ]]; then
        echo "You need to provide the name of a release from http://releases.sapsailing.com/"
        exit 1
    fi

    # Honor the no-overrite setting if there is one
    if [ -f $SERVER_HOME/no-overwrite ]; then
        echo "Found a no-overwrite file in the servers directory. Please remove it to complete this operation!"
    else
        load_from_release_file
        echo "ATTENTION: This new release code is not active yet. Make sure to restart the server if it is running!"
    fi

elif [[ $OPERATION == "install-local-release" ]]; then
    INSTALL_FROM_RELEASE=$PARAM
    if [[ $INSTALL_FROM_RELEASE == "" ]]; then
        echo "You need to provide the file of a release without tar.gz"
        exit 1
    fi

    # Honor the no-overrite setting if there is one
    if [ -f $SERVER_HOME/no-overwrite ]; then
        echo "Found a no-overwrite file in the servers directory. Please remove it to complete this operation!"
    else
        load_from_local_release_file
        echo "ATTENTION: This new release code is not active yet. Make sure to restart the server if it is running!"
    fi

elif [[ $OPERATION == "install-env" ]]; then
    USE_ENVIRONMENT=$PARAM
    if [[ $USE_ENVIRONMENT == "" ]]; then
        echo "You need to provide the name of an environment from http://releases.sapsailing.com/environments"
        exit 1
    fi

    if [ -f $SERVER_HOME/no-overwrite ]; then
        echo "Found a no-overwrite file in the servers directory. Please remove it to complete this operation!"
    else
        install_environment
        # make sure to reload data
        source `pwd`/env.sh

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
        echo "ATTENTION: This new configuration is not active yet. Make sure to restart the server if it is running!"
    fi
else
    echo "Script to prepare a Java instance running on Amazon."
    echo ""
    echo "install-release <release>: Downloads the release specified by the second option and overwrites all code for this server. Preserves env.sh."
    echo "install-local-release <release-file>: Installs the release file specified by the second option and overwrites all code for this server. Preserves env.sh."
    echo "install-env <environment>: Downloads and updates the environment with the one specified as a second option. Does NOT take into account Amazon user-data!"
    exit 0
fi

