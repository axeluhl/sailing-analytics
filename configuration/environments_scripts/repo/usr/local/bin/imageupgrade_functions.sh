#!/bin/bash

# Upgrades the AWS EC2 instance that this script is assumed to be executed on.
# The steps are as follows:

REBOOT_INDICATOR=/var/lib/sailing/is-rebooted
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

#DEPRECATED
update_root_crontab() {
  # The following assumes that /root/crontab is a symbolic link to /home/sailing/code/configuration/crontabs/<the crontab appropriate
  # to the environment or user>
  # which has previously been updated by a git pull:
  cd /root
  crontab crontab
}

build_crontab_and_setup_files() {
    scp -o StrictHostKeyChecking=no -r "wiki@sapsailing.com:~/gitwiki/configuration/environments_scripts" /root
    cd /root/
    chown root:root environments_scripts
    cd environments_scripts
    ./build-crontab-and-cp-files "${ENVIRONMENT_TYPE}" "${GIT_USER}" "${RELATIVE_PATH_TO_GIT}"
    cd ..
    rm -rf /root/environments_scripts
}

setup_keys() {
    #1: Environment type.
    SEPARATOR="@."
    ACTUAL_SYMBOL="@@"
    TEMP_KEY_DIR=$(mktemp  -d /root/keysXXXXX)
    REGION=$(TOKEN=`curl -X PUT "http://169.254.169.254/latest/api/token" --silent -H "X-aws-ec2-metadata-token-ttl-seconds: 21600"` \
    && curl -H "X-aws-ec2-metadata-token: $TOKEN" --silent http://169.254.169.254/latest/meta-data/placement/region)
    scp -pr root@sapsailing.com:/root/key_vault/${1}/* "${TEMP_KEY_DIR}"
    cd "${TEMP_KEY_DIR}"
    for user in $(ls); do 
        if id -u "$user"; then
            user_home_dir=$(getent passwd $(id -u "$user") | cut -d: -f6) # getent searches for passwd based on user id, which the "id" command supplies.
            # aws setup
            if [[ -d "${user}/aws" ]]; then 
                mkdir --parents "${user_home_dir}/.aws"
                chmod 766 "${user_home_dir}/.aws"
                chown -R  ${user}:${user} "${user_home_dir}/.aws"
                \cp -r --preserve --dereference "${user}"/aws/* "${user_home_dir}/.aws"
                sed -i "s/region = .*/region = ${REGION}/" "${user_home_dir}/.aws/config"
            fi
            # ssh setup
            if [[ -d "${user}/ssh" ]]; then
                mkdir --parents "${user_home_dir}/.ssh"
                chmod 700 "${user_home_dir}/.ssh"
                chown -R  ${user}:${user} "${user_home_dir}/.ssh"
                \cp --preserve --dereference $(find ${user}/ssh -maxdepth 1 -type f)  "${user_home_dir}/.ssh"
                for key in $(find ${user}/ssh/authorized_keys -type f); do
                    cat "${key}" >>  ${user_home_dir}/.ssh/authorized_keys
                done
            fi
        fi
    done
    cd /
    rm -rf "${TEMP_KEY_DIR}"
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
