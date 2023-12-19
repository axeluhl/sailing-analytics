#!/bin/bash

# Upgrades the AWS EC2 instance that this script is assumed to be executed on.
# The steps are as follows:

REBOOT_INDICATOR=/var/run/is-rebooted
LOGON_USER_HOME=/root

run_yum_update() {
  echo "Updating packages using yum" >>/var/log/sailing.err
  yum -y update
}

run_apt_update_upgrade() {
  echo "Updating packages using apt" >>/var/log/sailing.err
  apt-get -y update; apt-get -y upgrade
  apt-get -y install linux-image-cloud-amd64
  apt-get -y autoremove
}

run_git_pull() {
  echo "Pulling git to /home/sailing/code" >>/var/log/sailing.err
  su - sailing -c "cd code; git pull"
}

download_and_install_latest_sap_jvm_8() {
  echo "Downloading and installing latest SAP JVM 8 to /opt/sapjvm_8" >>/var/log/sailing.err
  vmpath=$( curl -s --cookie eula_3_1_agreed=tools.hana.ondemand.com/developer-license-3_1.txt https://tools.hana.ondemand.com | grep additional/sapjvm-8\..*-linux-x64.zip | head -1 | sed -e 's/^.*a href="\(additional\/sapjvm-8\..*-linux-x64\.zip\)".*/\1/' )
  if [ -n "${vmpath}" ]; then
    echo "Found VM version ${vmpath}; upgrading installation at /opt/sapjvm_8" >>/var/log/sailing.err
    if [ -z "${TMP}" ]; then
      TMP=/tmp
    fi
    echo "Downloading SAP JVM 8 as ZIP file to ${TMP}/sapjvm8-linux-x64.zip" >>/var/log/sailing.err
    curl --cookie eula_3_1_agreed=tools.hana.ondemand.com/developer-license-3_1.txt "https://tools.hana.ondemand.com/${vmpath}" > ${TMP}/sapjvm8-linux-x64.zip 2>>/var/log/sailing.err
    cd /opt
    rm -rf sapjvm_8
    if [ -f SIGNATURE.SMF ]; then
      rm -f SIGNATURE.SMF
    fi
    unzip ${TMP}/sapjvm8-linux-x64.zip >>/var/log/sailing.err
    rm -f ${TMP}/sapjvm8-linux-x64.zip
    rm -f SIGNATURE.SMF
  else
    echo "Did not find SAP JVM 8 at tools.hana.ondemand.com; not trying to upgrade" >>/var/log/sailing.err
  fi
}

clean_logrotate_target() {
  echo "Clearing logrorate-targets" >>/var/log/sailing.err
  rm -rf /var/log/logrotate-target/*
}

clean_httpd_logs() {
  echo "Clearing httpd logs" >>/var/log/sailing.err
  service httpd stop
  rm -rf /var/log/httpd/*
  rm -f /etc/httpd/conf.d/001-internals.conf
}

clean_startup_logs() {
  echo "Clearing bootstrap logs" >>/var/log/sailing.err
  rm -f /var/log/sailing*
  # Ensure that upon the next boot the reboot indicator is not present, indicating that it's the first boot
  rm "${REBOOT_INDICATOR}"
}

clean_servers_dir() {
  rm -rf /home/sailing/servers/*
}

update_root_crontab() {
  # The following assumes that /root/crontab is a symbolic link to /home/sailing/code/configuration/crontabs/<the crontab appropriate
  # to the environment or user>
  # which has previously been updated by a git pull:
  cd /root
  crontab crontab
}

clean_root_ssh_dir_and_tmp() {
  echo "Cleaning up ${LOGON_USER_HOME}/.ssh" >>/var/log/sailing.err
  rm -rf ${LOGON_USER_HOME}/.ssh/*
  rm -f /var/run/last_change_aws_landscape_managers_ssh_keys
  rm -rf /tmp/image-upgrade-finished
}

get_ec2_user_data() {
  /opt/aws/bin/ec2-metadata -d | sed -e 's/^user-data: //'
}

finalize() {
  # Finally, shut down the node unless "no-shutdown" was provided in the user data, so that a new AMI can be constructed cleanly
  if get_ec2_user_data | grep "^no-shutdown$"; then
    echo "Shutdown disabled by no-shutdown option in user data. Remember to clean /root/.ssh when done."
    touch /tmp/image-upgrade-finished
  else
    # Only clean ${LOGON_USER_HOME}/.ssh directory and /tmp/image-upgrade-finished if the next step is shutdown / image creation
    clean_root_ssh_dir_and_tmp
    rm -f /var/log/sailing.err
    shutdown -h now &
  fi
}
