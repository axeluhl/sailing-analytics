#!/bin/bash
# Uploads repository to centralized location
# Make sure to have called ./createLocalBaseP2RepositoryLinux.sh before
# and make sure to have activated local-p2-admin target to check
# if everything went well
# Call like this:
#   ./uploadSpecifedRepositoryToServer.sh {repo-name} {update-site-project-name}
# Examples:
#   ./uploadSpecifedRepositoryToServer.sh sailing com.sap.sailing.targetplatform.base
#   ./uploadSpecifedRepositoryToServer.sh aws-sdk com.amazon.aws.aws-java-api.updatesite

set -u

USER="trac"
SERVER="sapsailing.com"
REPO_HOME="/home/trac/p2-repositories"
PORT=22
REPO_NAME=$1
LOCAL_REPO_PROJECT=$2

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

echo "Performing backup of old repository to $REPO_HOME/${REPO_NAME}.backup.$DATE..."
ssh -p $PORT $USER@$SERVER "cp -r $REPO_HOME/${REPO_NAME} $REPO_HOME/${REPO_NAME}.backup.$DATE"

echo "Removing old p2-repository"
ssh -p $PORT $USER@$SERVER "rm -rf $REPO_HOME/${REPO_NAME}"

echo "Uploading local repository to $REPO_HOME/${REPO_NAME}"
scp -P $PORT -r ../../${LOCAL_REPO_PROJECT}/target/repository $USER@$SERVER:$REPO_HOME/${REPO_NAME}

echo "Making readable for everyone..."
ssh -p $PORT $USER@$SERVER "chmod -R 775 $REPO_HOME/${REPO_NAME}"

echo "Remote files now available:"
ssh -p $PORT $USER@$SERVER "ls -lah $REPO_HOME/${REPO_NAME}"

echo ""
echo "Make SURE to notify developers that they need to update their target platform!!!"
