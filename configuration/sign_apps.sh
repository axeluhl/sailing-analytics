#!/bin/sh

find_project_home ()
{
    if [[ "$1" == '/' ]] || [[ "$1" == "" ]]; then
        echo ""
        return 0
    fi

    if [ ! -d "$1/.git" ]; then
        PARENT_DIR="`cd "$1/..";pwd`"
        OUTPUT=$(find_project_home "$PARENT_DIR")

        if [ "$OUTPUT" = "" ] && [ -d "$PARENT_DIR/$CODE_DIRECTORY" ] && [ -d "$PARENT_DIR/$CODE_DIRECTORY/.git" ]; then
            OUTPUT="$PARENT_DIR/$CODE_DIRECTORY"
        fi
        echo $OUTPUT
        return 0
    fi

    echo $1 | sed -e 's/\/cygdrive\/\([a-zA-Z]\)/\1:/'
}

sign_apk()
{
    ALIAS=$1
    PROJECT_NAME=$2
    PROJECT_UNALIGNED=$PROJECT_NAME-unaligned.apk

    cd $PROJECT_HOME/mobile/$PROJECT_NAME/build/outputs/apk
    rm $PROJECT_NAME.apk
    cp $PROJECT_NAME-release-unsigned.apk $PROJECT_UNALIGNED

    jarsigner -verbose -tsa -sigalg SHA1withRSA -digestalg SHA1 -keystore $PROJECT_HOME/mobile/android-keystore/STG.jks $PROJECT_UNALIGNED $ALIAS
    $ANDROID_HOME/build-tools/$BUILD_TOOLS/zipalign -v 4 $PROJECT_UNALIGNED $PROJECT_NAME.apk
}

if [ $# -eq 0 ]; then
    echo "Usage: $0 <key_alias>"
    exit 2
fi

# this holds for default installation
BUILD_TOOLS="21.1.2"
USER_HOME=~
START_DIR="`pwd`"

if [ "$PROJECT_HOME" = "" ]; then
    PROJECT_HOME=$(find_project_home "$START_DIR")
fi

# if project_home is still empty we could not determine any suitable directory
if [[ "$PROJECT_HOME" == "" ]]; then
    echo "Could neither determine nor get PROJECT_HOME. Please provide it by setting an environment variable with this name."
    exit 1
fi

sign_apk $1 com.sap.sailing.android.tracking.app
sign_apk $1 com.sap.sailing.racecommittee.app
