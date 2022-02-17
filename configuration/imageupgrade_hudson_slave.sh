#!/bin/bash

# Upgrades the AWS EC2 Hudson Build Slave instance that this script is assumed to be executed on.
# The steps are as follows:

. imageupgrade_functions.sh

get_ec2_user_data() {
  /usr/bin/ec2metadata --user
}

LOGON_USER_HOME=/home/ubuntu

run_apt_update_upgrade
download_and_install_latest_sap_jvm_8
run_git_pull
finalize
