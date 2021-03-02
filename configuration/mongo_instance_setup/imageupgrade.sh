#!/bin/bash

# Upgrades the AWS EC2 MongoDB instance that this script is assumed to be executed on.
# The steps are as follows:

. imageupgrade_functions.sh

run_yum_update
run_git_pull
clean_startup_logs
update_root_crontab
finalize
