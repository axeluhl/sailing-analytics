#!/bin/bash

# Upgrades the AWS EC2 MongoDB instance that this script is assumed to be executed on.
# The steps are as follows:

. imageupgrade_functions.sh

run_git_pull_root() {
  echo "Pulling git to /root/code" >>/var/log/sailing.err
  cd /root/code
  git pull
}

clean_mongo_pid() {
  rm -f /var/run/mongodb/mongod.pid
}

LOGON_USER_HOME=/home/ec2-user

run_yum_update
build_crontab_and_setup_files mongo_instance_setup root code
clean_startup_logs
clean_mongo_pid
finalize
