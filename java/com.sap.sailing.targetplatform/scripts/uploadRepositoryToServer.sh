#!/bin/bash
# Uploads repository to centralized location
# Make sure to have called ./createLocalBaseP2RepositoryLinux.sh before
# and make sure to have activated local-p2-admin target to check
# if everything went well

set -u

USER="trac"
SERVER="sapsailing.com"
REPO_HOME="/home/trac/p2-repositories"
PORT=22

DATE=`date +%s`

options='u:h:p:'
while getopts $options option
do
    case $option in
	u) USER=$OPTARG;;
	p) PORT=$OPTARG;;
	h) SERVER=$OPTARG;;
        \?) echo "Invalid option"
            exit 4;;
    esac
done

echo "Performing backup of old repository to $REPO_HOME/sailing.backup.$DATE..."
ssh -p $PORT $USER@$SERVER "cp -r $REPO_HOME/sailing $REPO_HOME/sailing.backup.$DATE"

echo "Removing old p2-repository"
ssh -p $PORT $USER@$SERVER "rm -rf $REPO_HOME/sailing"

echo "Uploading local repository to $REPO_HOME/sailing"
scp -P $PORT -r ../../com.sap.sailing.targetplatform.base/target/repository $USER@$SERVER:$REPO_HOME/sailing

echo "Making readable for everyone..."
ssh -p $PORT $USER@$SERVER "chmod -R 775 $REPO_HOME/sailing"

echo "Remote files now available:"
ssh -p $PORT $USER@$SERVER "ls -lah $REPO_HOME/sailing"

echo ""
echo "Make SURE to notify developers that they need to update their target platform!!!"
