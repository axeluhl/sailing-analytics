#!/bin/bash

# Upgrades the AWS EC2 instance that this script is assumed to be executed on.
# The steps are as follows:

REBOOT_INDICATOR=/var/run/is-rebooted

run_yum_update() {
  echo "Updating packages using yum" >>/var/log/sailing.err
  yum -y update
}

run_git_pull() {
  echo "Pulling git to /home/sailing/code" >>/var/log/sailing.err
  su - sailing -c "cd code; git pull"
}

run_refresh_instance_install_release() {
  echo "Upgrading sailing server software" >>/var/log/sailing.err
  su - sailing -c "cd servers; for i in *; do echo \"Upgrading \$i\"; cd \$i; echo \"Updating release in \`pwd\`\"; ./refreshInstance.sh install-release; cd ..; done" >>/var/log/sailing.err
}

clean_logrotate_target() {
  echo "Clearing logrorate-targets" >>/var/log/sailing.err
  rm -rf /var/log/logrotate-target/*
}

clean_httpd_logs() {
  echo "Clearing httpd logs" >>/var/log/sailing.err
  service httpd stop
  rm -rf /var/log/httpd/*
  rm /etc/httpd/conf.d/001-internals.conf
}

clean_sailing_logs() {
  echo "Clearing sailing logs" >>/var/log/sailing.err
  rm -rf /home/sailing/servers/*/logs/*
}

clean_startup_logs() {
  echo "Clearing bootstrap logs" >>/var/log/sailing.err
  rm /var/log/sailing*
  # Ensure that upon the next boot the reboot indicator is not present, indicating that it's the first boot
  rm "${REBOOT_INDICATOR}"
}

run_yum_update
run_git_pull
run_refresh_instance_install_release
clean_logrotate_target
clean_httpd_logs
clean_sailing_logs
clean_startup_logs

# Finally, shut down the node unless "no-shutdown" was provided in the user data, so that a new AMI can be constructed cleanly
if /opt/aws/bin/ec2-metadata -d | grep "^no-shutdown$"; then
  echo "Shutdown disabled by no-shutdown option in user data"
  touch /tmp/image-upgrade-finished
else
  shutdown -h now &
fi
