#!/bin/bash
REMOTE=$1
cd /etc/httpd
git status
if [[ "$?" -ne 0 ]]; then
    rm -rf .git
    rm -rf conf   # Perhaps, this script should instead mv to a backup location
    rm -rf conf.d
    rm -rf .gitignore
    git init
    git remote add origin "${REMOTE}"
    GIT_SSH_COMMAND="ssh -A -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"  git fetch 
    git checkout main
fi

