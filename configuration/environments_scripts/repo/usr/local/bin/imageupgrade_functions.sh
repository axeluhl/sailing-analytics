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
  vmpath=$( curl -s --cookie eula_3_2_agreed=tools.hana.ondemand.com/developer-license-3_2.txt https://tools.hana.ondemand.com | grep additional/sapjvm-8\..*-linux-x64.zip | head -1 | sed -e 's/^.*a href="\(additional\/sapjvm-8\..*-linux-x64\.zip\)".*/\1/' )
  if [ -n "${vmpath}" ]; then
    echo "Found VM version ${vmpath}; upgrading installation at /opt/sapjvm_8" >>/var/log/sailing.err
    if [ -z "${TMP}" ]; then
      TMP=/tmp
    fi
    echo "Downloading SAP JVM 8 as ZIP file to ${TMP}/sapjvm8-linux-x64.zip" >>/var/log/sailing.err
    curl --cookie eula_3_2_agreed=tools.hana.ondemand.com/developer-license-3_2.txt "https://tools.hana.ondemand.com/${vmpath}" > ${TMP}/sapjvm8-linux-x64.zip 2>>/var/log/sailing.err
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
    if [[ "$#" -eq 0 ]]; then
        echo "Number of arguments is invalid. Please use the options and arguments as follows."
        echo "Options for this function:"
        echo "  -h is the hostname to fetch the configuration/environments_scripts from."
        echo "The remaining (optional) args and options, if correct, are passed to the build_crontab_and_setup_files script."
        echo "  -c means no crontab file is created"
        echo "  -n means that if a crontab has been created, it isn't actually installed. This is useful for testing."
        echo "  -f means no files are copied over, which is useful if you have already copied files accross or don't want to override existing files"
        echo "Then there are the arguments, where the order matters:"
        echo "  ENVIRONMENT_TYPE - the directory name in environments_scripts which will be used."
        echo "  USER_WITH_COPY_OF_REPO - a user which will exist on the environment type, which has a checked out copy of the git workspace."
        echo "  RELATIVE_PATH_OF_GIT_DIR_WITHIN_USER - the relative path within the USER_WITH_COPY_OF_REPO to get to the git workspace."
    else
        TEMP=$(getopt -o fnch: -n 'options checker' -- "$@")
        [[ "$?" -eq 0 ]] || return 2
        eval set -- "$TEMP"
        PASS_OPTIONS=()
        HOSTNAME="sapsailing.com"
        while :; do
            case "$1" in
                -c|-f|-n)
                    PASS_OPTIONS+=("$1")
                    ;;
                -h)
                    if [[ "$2" ]]; then
                        HOSTNAME="$2"
                        shift
                    else
                        echo "hostname option requires argument"
                    fi
                    ;;
                --)
                    shift
                    break
                    ;;
                *)
                    echo "no more options"
                    break
            esac
            shift
        done
        TEMP_ENVIRONMENTS_SCRIPTS=$(mktemp -d /var/tmp/environments_scripts_XXX)
        echo "Attempting access to the wiki, typically used by image upgrade. Otherwise, try root. NOTE: If the first command fails, there will be a warning message."
        scp -o StrictHostKeyChecking=no -pr wiki@"$HOSTNAME":~/gitwiki/configuration/environments_scripts/* "${TEMP_ENVIRONMENTS_SCRIPTS}"
        [[ "$?" -eq 0 ]] || scp -o StrictHostKeyChecking=no -pr root@"$HOSTNAME":/home/wiki/gitwiki/configuration/environments_scripts/* "${TEMP_ENVIRONMENTS_SCRIPTS}" # For initial setup as not all landscape managers have direct wiki access.
        cd "${TEMP_ENVIRONMENTS_SCRIPTS}"
        # Add all args to array, otherwise, if PASS_OPTIONS is empty, and we also pass $@ then argument $1 is in fact null, which would cause errors.
        for option in "$@"; do
        PASS_OPTIONS+=( "$option")
        done
        if ! sudo  ./build-crontab-and-cp-files "${PASS_OPTIONS[@]}"; then
            return 1
        fi
        cd ..
        rm -rf "$TEMP_ENVIRONMENTS_SCRIPTS"
    fi
}

setup_keys() {
    #1: Environment type.
    # Optional parameter is -p which indicates that no permissions will be set or overwritten.
    TEMP=$(getopt -o p -n 'options' -- "$@")
    [[ "$?" -eq 0 ]] || return 2
    SET_PERMISSIONS="true"
    eval set -- "$TEMP"
    while true; do
        case "$1" in
            -p)
                SET_PERMISSIONS="false"
                ;;
            --)
                shift
                break
                ;;
            *)
                echo "Option not recognised"
                return 2
                ;;
        esac
        shift
    done
    if [[ "$#" -ne 1 ]]; then
        echo "Please specify the environment type and use the optional -p flag to indicate that no permissions will be set or overwritten."
        return 2
    fi
    pushd .
    TEMP_KEY_DIR=$(mktemp  -d /root/keysXXXXX)
    REGION=$(TOKEN=`curl -X PUT "http://169.254.169.254/latest/api/token" --silent -H "X-aws-ec2-metadata-token-ttl-seconds: 21600"` \
    && curl -H "X-aws-ec2-metadata-token: $TOKEN" --silent http://169.254.169.254/latest/meta-data/placement/region)
    scp -o StrictHostKeyChecking=no -pr root@sapsailing.com:/root/new_version_key_vault/"${1}"/* "${TEMP_KEY_DIR}"
    cd "${TEMP_KEY_DIR}"
    for user in *; do
        [[ -e "$user" ]] || continue
        if id -u "$user" > /dev/null; then
            user_home_dir=$(getent passwd $(id -u "$user") | cut -d: -f6) # getent searches for passwd based on user id, which the "id" command supplies.
            # aws setup
            if [[ -d "${user}/aws" ]]; then 
                mkdir --parents "${user_home_dir}/.aws"
                # Setup credentials
                if [[ -d "${user}/aws/credentials" && ! -e "${user_home_dir}/.aws/credentials" ]]; then
                    > "${user_home_dir}"/.aws/credentials
                    for credentials in "${user}"/aws/credentials/*; do
                        [[ -f "$credentials" ]] || continue
                        cat "$credentials" >> "${user_home_dir}"/.aws/credentials
                        echo "" >> "${user_home_dir}"/.aws/credentials
                    done
                fi
                # Setup config
                if [[ ! -e "${user_home_dir}/.aws/config" ]]; then
                    echo "[default]" >> "${user_home_dir}/.aws/config"
                    echo "region = ${REGION}" >> "${user_home_dir}"/.aws/config
                    echo "" >> "${user_home_dir}"/.aws/config
                    if [[ -d "${user}/aws/config" ]]; then
                        for config in "${user}"/aws/config/*; do
                            [[ -f "$config" ]] || continue
                            cat "$config" >> "${user_home_dir}"/.aws/config
                            echo "region = ${REGION}" >> "${user_home_dir}"/.aws/config
                            echo "" >> "${user_home_dir}"/.aws/config
                        done
                    fi
                fi
                if [[ "$SET_PERMISSIONS" == "true" ]]; then
                    chmod 755 "${user_home_dir}"/.aws
                    chown -R  ${user}:${user} "${user_home_dir}/.aws"
                    chmod 600 "${user_home_dir}"/.aws/*
                fi
            fi
            # ssh setup
            if [[ -d "${user}/ssh" ]]; then
                mkdir --parents "${user_home_dir}/.ssh"
                for key in "${user}"/ssh/*; do
                    [[ -f "$key" ]] || continue
                    [[ ! -f "$user_home_dir"/.ssh/"$(basename "$key")" ]] || continue
                    \cp --preserve --dereference "$key" "$user_home_dir"/.ssh
                done
                for key in "${user}"/ssh/authorized_keys/*; do
                    [[ -f "$key" ]] || continue
                    if ! grep -q "$(cat "$key")" "${user_home_dir}"/.ssh/authorized_keys; then
                        cat "${key}" >>  "${user_home_dir}"/.ssh/authorized_keys
                    fi
                done
                if [[ "$SET_PERMISSIONS" == "true" ]]; then
                    chmod 700 "${user_home_dir}/.ssh"
                    chown -R  ${user}:${user} "${user_home_dir}/.ssh"
                    chmod 600 "${user_home_dir}"/.ssh/*
                fi
            fi
        fi
    done
    popd
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

setup_cloud_cfg_and_root_login() {
    sed -i 's/#PermitRootLogin yes/PermitRootLogin without-password\nPermitRootLogin yes/' /etc/ssh/sshd_config
    sed -i 's/^disable_root: true$/disable_root: false/' /etc/cloud/cloud.cfg
    echo "preserve_hostname: true" >> /etc/cloud/cloud.cfg
}

setup_fail2ban() {
    pushd .
    if [[ ! -f "/etc/systemd/system/fail2ban.service" ]]; then 
        yum install 2to3 -y
        cd /usr/local/src
        wget https://github.com/fail2ban/fail2ban/archive/refs/tags/1.0.2.tar.gz
        tar -xvf 1.0.2.tar.gz
        cd fail2ban-1.0.2/
        ./fail2ban-2to3
        python3.9 setup.py build
        python3.9 setup.py install
        cp ./build/fail2ban.service /etc/systemd/system/fail2ban.service
        sed -i 's|Environment=".*"|Environment="PYTHONPATH=/usr/local/lib/python3.9/site-packages"|' /etc/systemd/system/fail2ban.service
        systemctl enable fail2ban
        chkconfig --level 23 fail2ban on
    fi
    cat << EOF > /etc/fail2ban/jail.d/customisation.local
    [ssh-iptables]

    enabled  = true
    filter   = sshd[mode=aggressive]
    action   = iptables[name=SSH, port=ssh, protocol=tcp]
            sendmail-whois[name=SSH, dest=axel.uhl@sap.com, sender=fail2ban@sapsailing.com]
    logpath  = /var/log/fail2ban.log
    maxretry = 5
EOF
    touch /var/log/fail2ban.log
    service fail2ban start
    yum remove -y firewalld
    popd
}

setup_mail_sending() {
    yum install -y mailx postfix
    systemctl enable postfix
    temp_mail_properties_location=$(mktemp /root/mail.properties_XXX)
    scp -o StrictHostKeyChecking=no  -p root@sapsailing.com:mail.properties "${temp_mail_properties_location}"
    cd $(dirname "${temp_mail_properties_location}")
    local smtp_host="$(sed -n "s/mail.smtp.host \?= \?\(.*\)/\1/p" ${temp_mail_properties_location})"
    local smtp_port="$(sed -n "s/mail.smtp.port \?= \?\(.*\)/\1/p" ${temp_mail_properties_location})"
    local smtp_user="$(sed -n "s/mail.smtp.user \?= \?\(.*\)/\1/p" ${temp_mail_properties_location})"
    local smtp_pass="$(sed -n "s/mail.smtp.password \?= \?\(.*\)/\1/p" ${temp_mail_properties_location})"
    local password_file_location="/etc/postfix/sasl_passwd"
    echo "relayhost = [${smtp_host}]:${smtp_port}
smtp_sasl_auth_enable = yes
smtp_sasl_security_options = noanonymous
smtp_sasl_password_maps = hash:${password_file_location}
smtp_use_tls = yes
smtp_tls_security_level = encrypt
smtp_tls_note_starttls_offer = yes

myorigin =\$myhostname.sapsailing.com
" >> /etc/postfix/main.cf
    sed -i  "/smtp_tls_security_level = may/d" /etc/postfix/main.cf
    echo "[${smtp_host}]:${smtp_port} ${smtp_user}:${smtp_pass}" >> ${password_file_location}
    postmap hash:${password_file_location}
    systemctl restart postfix
    rm -f "${temp_mail_properties_location}"
}

setup_sshd_resilience() {
    sudo su -c "echo 'ClientAliveInterval 3
ClientAliveCountMax 3
GatewayPorts yes' >> /etc/ssh/sshd_config && systemctl reload sshd.service"
}

identify_suitable_partition_for_ephemeral_volume() {
    EPHEMERAL_VOLUME_NAME=$(
    # List all block devices and find those named nvme...
    for i in $(lsblk | grep -o "nvme[0-9][0-9]\?n[0-9]" | sort -u); do
        # If they don't have any partitions, then...
        if ! lsblk | grep -o "${i}p[0-9]\+" 2>&1 >/dev/null; then
            # ...check whether they are EBS devices
            /sbin/ebsnvme-id -u "/dev/$i" >/dev/null
            # If not, list their name because then they must be ephemeral instance storage
            if [[ $? -ne 0 ]]; then
                echo "${i}"
            fi
        fi
    done 2>/dev/null | head -n 1 )
    echo $EPHEMERAL_VOLUME_NAME
}

setup_goaccess() {
    # Compatible with Amazon Linux 2023
    pushd .
    cd /usr/local/src
    wget https://tar.goaccess.io/goaccess-1.9.1.tar.gz
    tar -xzvf goaccess-1.9.1.tar.gz
    cd goaccess-1.9.1/
    yum install -y gcc-c++
    yum install -y libmaxminddb-devel ncurses-devel
    ./configure --enable-utf8
    make
    make install
    # old location:
    # scp root@sapsailing.com:/etc/goaccess.conf /usr/local/etc/goaccess/goaccess.conf
    # once we switch from amazon linux 1:
    scp root@sapsailing.com:/usr/local/etc/goaccess/goaccess.conf /usr/local/etc/goaccess/goaccess.conf
    popd
}
setup_apachetop() {
    # Compatible with Amazon Linux 2023
    pushd .
    yum install -y gcc-c++
    yum install -y ncurses-devel readline-devel
    cd /usr/local/src
    wget https://github.com/tessus/apachetop/releases/download/0.23.2/apachetop-0.23.2.tar.gz
    tar -xvzf apachetop-0.23.2.tar.gz
    cd apachetop-0.23.2
    ./configure
    make
    make install
    popd
}

setup_swap() {
    # $1: size of swapspace in megabytes.
    echo "Creating swapswpace of $1 MBs"
    local swapfile_location=/var/cache/swapfile
    pushd .
    sudo dd if=/dev/zero of="$swapfile_location" bs=1M count="$1"
    sudo chmod 600 "$swapfile_location"
    sudo chown root:root "$swapfile_location"
    sudo mkswap "$swapfile_location"
    sudo su - -c "echo \"$swapfile_location       none    swap    pri=0      0       0\" >> /etc/fstab"
    sudo swapon -a
    popd
}
