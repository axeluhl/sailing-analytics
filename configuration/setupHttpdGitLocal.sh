#!/bin/bash
git status
if [[ $? -eq 0 ]]; then 
    git init
    git branch -m main
    git remote add origin httpdGit@18.135.5.168:/home/httpdGit/mainHttpdConfig.git
    git add *
    git pull --set-upstream origin main
else 
    echo "fail"
fi
