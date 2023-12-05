#!/bin/bash

# Purpose: This script goes to a given git dir (eg. httpd); fetches any new commits to
# the repo; and - if new commits are found - merges them into the branch and runs a command.

if [ $# -eq 0 ]; then
    echo "$0 PATH_TO_GIT_REPO COMMAND_TO_RUN_ON_COMPLETION_IN_REPO"
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

