#!/bin/bash
REMOTE=$1
cd /etc/httpd
git status
if [[ $? -eq 0 ]]; then 
    rm -rf conf   # Perhaps, this script should instead mv to a backup location
    rm -rf conf.d
    git init
    git branch -m main
    git remote add origin "${REMOTE}"
    git checkout main
else 
    echo "fail"
fi
