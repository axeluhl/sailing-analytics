
# When running on Amazon this script executes the necessary steps
# to build or install an instance

. `pwd`/env.sh

if [[ $BUILD_BEFORE_START == "True" ]] || [[ $INSTALL_FROM_RELEASE != "" ]]; then

    # check for available memory - build can not be started with less than 1GB
    MEM_TOTAL=`free -mt | grep Total | awk '{print $2}'`
    if [ $MEM_TOTAL -lt 924 ]; then
        echo "Could not start build process with less than 1GB of RAM!"
        echo "Not enough RAM for completing the build process! You need at least 1GB. Instance NOT started!" | mail -r simon.marcel.pamies@sap.com -s "Build of $INSTANCE_ID failed" $BUILD_COMPLETE_NOTIFY
        exit 10
    fi

    if [[ $INSTALL_FROM_RELEASE != "" ]]; then
        echo "Build/Deployment process has been started - it can take 5 to 20 minutes until your instance is ready. " | mail -r simon.marcel.pamies@sap.com -s "Build or Deployment of $INSTANCE_ID starting" $BUILD_COMPLETE_NOTIFY
    fi

    `pwd`/shouldIBuildOrShouldIGo.sh &> last_automatic_build.txt
     STATUS=$?
     if [ $STATUS -eq 0 ]; then
        echo "Deployment Successful"
        echo "OK - check the attachment for more information." | mail -a last_automatic_build.txt -r simon.marcel.pamies@sap.com -s "Build or Deployment of $INSTANCE_ID complete" $BUILD_COMPLETE_NOTIFY
     else
        echo "Deployment Failed"
        echo "ERROR - check the attachment for more information." | mail -a last_automatic_build.txt -r simon.marcel.pamies@sap.com -s "Build of $INSTANCE_ID failed" $BUILD_COMPLETE_NOTIFY
        exit
     fi 
fi
