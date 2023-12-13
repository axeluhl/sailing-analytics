#!/bin/bash
REMOTE=$1
cd /etc/httpd

rm -rf conf   # Perhaps, this script should instead mv to a backup location
rm -rf conf.d
git init
git remote add origin "${REMOTE}"
git fetch -o "StrictHostKeyChecking=no"
git checkout main

