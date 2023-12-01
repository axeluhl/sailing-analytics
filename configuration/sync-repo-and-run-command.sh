#!/bin/bash

# Purpose: This script is scheduled as a cronjob to periodically go to a given git dir (eg. httpd); fetch any new commits to
# the repo; and - if new commits are found - merge them into the branch and run a command.
# Crontab for every 2 mins: */2 * * * * /path/to/updateHttpd.sh

if [ $# -eq 0 ]; then
    echo "$0 PATH_TO_GIT_REPO COMMAND_TO_RUN_ON_COMPLETION"
    echo ""
    echo "Script used to automatically fetch from a git repo and, if there are new commits, merge the changes."
    exit 2
fi

GIT_PATH=$1 # "/etc/httpd"
COMMAND_ON_COMPLETION=$2 # "sudo service httpd reload"

# rev-parse gets the commit hash of given reference.
CURRENT=$(cd ${GIT_PATH} && git rev-parse HEAD)
cd ${GIT_PATH} && git fetch
if [[ $CURRENT != $(git rev-parse origin/main) ]]
then
    logger -t httpd "Changes found; merging now"
    cd ${GIT_PATH} && git merge origin/main # fastforward merge occurs
    sleep 2
    $($COMMAND_ON_COMPLETION)
fi


