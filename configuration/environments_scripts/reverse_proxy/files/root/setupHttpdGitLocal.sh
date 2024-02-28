#!/bin/bash
REMOTE=$1
STATUS_DEFINITION_FILE="internal-server-status.conf"
SELF_IP=$( ec2-metadata --local-ipv4 | grep   -o "[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\>")
cd /etc/httpd
if ! git status; then
    echo "Currently not a git repo, creating and pulling latest changes"
    rm -rf .git
    rm -rf conf   # Perhaps, this script should instead mv to a backup location
    rm -rf conf.d
    rm -rf .gitignore
    git init
    git remote add origin "${REMOTE}"
    GIT_SSH_COMMAND="ssh -A -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"  git fetch 
    git checkout main
fi
echo "Use Status ${SELF_IP} internal-server-status" > /etc/httpd/conf.d/${STATUS_DEFINITION_FILE}