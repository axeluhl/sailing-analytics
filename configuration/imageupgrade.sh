#!/bin/bash

# Upgrades the AWS EC2 instance that this script is assumed to be executed on.
# The steps are as follows:

. `dirname $0`/imageupgrade_functions.sh

run_yum_update
run_git_pull
download_and_install_latest_sap_jvm_8
clean_logrotate_target
clean_httpd_logs
clean_servers_dir
clean_startup_logs
update_root_crontab
finalize
