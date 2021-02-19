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

clean_startup_logs() {
  echo "Clearing bootstrap logs" >>/var/log/sailing.err
  rm /var/log/sailing*
  # Ensure that upon the next boot the reboot indicator is not present, indicating that it's the first boot
  rm "${REBOOT_INDICATOR}"
}

clean_servers_dir() {
  rm -rf /home/sailing/servers/*
}

update_root_crontab() {
  # The following assumes that /root/crontab is a symbolic link to /home/sailing/code/configuration/crontab
  # which has previously been updated by a git pull:
  cd /root
  crontab crontab
}

clean_root_ssh_dir_and_tmp() {
  echo "Cleaning up /root/.ssh" >>/var/log/sailing.err
  rm -rf /root/.ssh/*
  rm -rf /tmp/image-upgrade-finished
}

run_yum_update
run_git_pull
clean_logrotate_target
clean_httpd_logs
clean_servers_dir
clean_startup_logs
update_root_crontab

# Finally, shut down the node unless "no-shutdown" was provided in the user data, so that a new AMI can be constructed cleanly
if /opt/aws/bin/ec2-metadata -d | grep "^no-shutdown$"; then
  echo "Shutdown disabled by no-shutdown option in user data. Remember to clean /root/.ssh when done."
  touch /tmp/image-upgrade-finished
else
  # Only clean root's .ssh directory and /tmp/image-upgrade-finished if the next step is shutdown / image creation
  clean_root_ssh_dir_and_tmp
  shutdown -h now &
fi
