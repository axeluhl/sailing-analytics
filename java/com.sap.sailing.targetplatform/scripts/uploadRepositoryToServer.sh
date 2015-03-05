#!/bin/bash
# Uploads repository to centralized location
# Make sure to have called ./createLocalBaseP2RepositoryLinux.sh before
# and make sure to have activated local-p2-admin target to check
# if everything went well

set -u

USER="trac"
SERVER="sapsailing.com"
REPO_HOME="/home/trac/p2-repositories"

DATE=`date +%s`

echo "Performing backup of old repository to $REPO_HOME/sailing.backup.$DATE..."
ssh $USER@$SERVER "cp -r $REPO_HOME/sailing $REPO_HOME/sailing.backup.$DATE"

echo "Removing old p2-repository"
ssh $USER@$SERVER "rm -rf $REPO_HOME/sailing"

echo "Uploading local repository to $REPO_HOME/sailing"
scp -r ../../com.sap.sailing.targetplatform.base/target/repository $USER@$SERVER:$REPO_HOME/sailing

echo "Making readable for everyone..."
ssh $USER@$SERVER "chmod -R a+r $REPO_HOME/sailing"
ssh $USER@$SERVER "chmod -R a+x $REPO_HOME/sailing"

echo "Remote files now available:"
ssh $USER@$SERVER "ls -lah $REPO_HOME/sailing"

echo ""
echo "Make SURE to notify developers that they need to update their target platform!!!"
