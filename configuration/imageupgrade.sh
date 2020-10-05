#!/bin/bash

# Upgrades the AWS EC2 instance that this script is assumed to be executed on.
# The steps are as follows:

run_yum_update() {
  yum -y update
}

run_git_pull() {
  su - sailing -c "cd code; git fetch; git merge origin/master"
}

run_refresh_instance_install_release() {
  su - sailing -c "cd servers; for i in *; do echo \"Upgrading \$i\"; cd \$i; echo \"Updating release in \`pwd\`\"; ./refreshInstance.sh install-release; cd ..; done"
}

clean_httpd_logs() {
  rm -rf /var/log/httpd/*
}

clean_sailing_logs() {
  rm -rf /home/sailing/servers/*/logs/*
}

run_yum_update
run_git_pull
run_refresh_instance_install_release
clean_httpd_logs
clean_sailing_logs

# Finally, shut down the node so that a new AMI can be constructed cleanly
shutdown -h now &
