#!/bin/bash

# Purpose: This script goes to a given git dir (eg. httpd); fetches any new commits to
# the repo; and - if new commits are found - merges them into the branch and runs a command.

if [ $# -eq 0 ]; then
    echo "$0 PATH_TO_GIT_REPO COMMAND_TO_RUN_ON_COMPLETION_IN_REPO"
    echo ""
    echo "EXAMPLE: sync-repo-and-execute-cmd.sh \"/etc/httpd\"  \"sudo service httpd reload\""
    echo "This script is used to automatically fetch from a git repo and, if there are new commits, merge the changes."
    echo "And then run a command, passed as an argument."
    exit 2
fi

GIT_PATH=$1
COMMAND_ON_COMPLETION=$2
cd ${GIT_PATH}
# Rev-parse gets the commit hash of given reference.
CURRENT_HEAD=$(git rev-parse HEAD)
git fetch
if [[ "$?" -ne 0 ]]; then
    exit 1
fi
if [[ $CURRENT_HEAD != $(git rev-parse origin/main) ]]  # Checks if there are new commits 
then
    logger -t httpd "Changes found; merging now"
    cd ${GIT_PATH} && git merge origin/main
    if [[ $? -eq 0 ]]; then
        logger -t httpdMerge "Merge succeeded: different files edited."
    else
        logger -t httpdMerge "First merge unsuccessful: same file modified."
        git merge --abort   # Returns to pre-merge state.
        git stash
        git merge origin/main # This should be a fast-forward merge.
        git stash apply  # Keeps stash on top of stack, in case the apply fails.
        if [[ $? -eq 0 ]]; then
            logger -t httpdMerge "Second merge success: merge of httpd remote to local successful, and previous working directory changes restored."
            git stash drop  # Removes successful stash from stash stack.
        else
            logger -t httpdMerge "Second merge unsuccessful: same sections modified"
            echo "Merging issue at commit $(git rev-parse HEAD). Currently at last safe commit." | notify-operators "Merge conflict on httpd instance. Manual intervention required."
            # Returns to pre-pull state and then pops
            git reset --hard "${CURRENT_HEAD}"
            git stash pop
            exit 1
        fi
    fi
    sleep 2
    $($COMMAND_ON_COMPLETION)
fi

