#!/bin/bash
if [ $# != 0 ]; then
  SERVER=$1
  scp "${0}" ec2-user@${SERVER}:
  ssh -A ec2-user@${SERVER} ./$( basename "${0}" )
else
  if ec2-metadata | grep -q instance-id; then
    echo "Running on an AWS EC2 instance as user ${USER} / $(whoami), starting setup..."
    # Allow root ssh login with the same key used for the ec2-user for now;
    # later, a cron job will be installed that keeps the /root/authorized_keys file
    # up to date with all landscape managers' public SSH keys
    sudo cp /home/ec2-user/.ssh/authorized_keys /root/.ssh
    sudo chown root /root/.ssh/authorized_keys
    sudo chgrp root /root/.ssh/authorized_keys
    sudo adduser sailing
    sudo su - sailing -c "mkdir servers"
    # Create an SSH key pair with empty passphrase for ec2-user, deploy it to trac@sapsailing.com
    # and then move it to the sailing user's .ssh directory
    ssh-keygen -t ed25519 -P '' -f /home/ec2-user/.ssh/id_ed25519
    cat /home/ec2-user/.ssh/id_ed25519.pub | ssh root@sapsailing.com "cat >>/home/trac/.ssh/authorized_keys"
    sudo mkdir /home/sailing/.ssh
    sudo mv /home/ec2-user/.ssh/id* /home/sailing/.ssh
    sudo chown -R sailing /home/sailing/.ssh
    sudo chgrp -R sailing /home/sailing/.ssh
    sudo chmod 700 /home/sailing/.ssh
    # Install standard packages:
    sudo yum -y update
    sudo yum -y install git tmux nvme-cli chrony
    # Force acceptance of sapsailing.com's host key:
    sudo su - sailing -c "ssh -o StrictHostKeyChecking=false trac@sapsailing.com ls" >/dev/null
    # Clone Git to /home/sailing/code
    sudo su - sailing -c "git clone ssh://trac@sapsailing.com/home/trac/git code"
    # Install SAP JVM 8:
    sudo mkdir -p /opt
    sudo su - -c "source /home/sailing/code/configuration/imageupgrade_functions.sh; download_and_install_latest_sap_jvm_8"
    # Install sailing.sh script to /etc/profile.d
    sudo ln -s /home/sailing/code/configuration/sailing.sh /etc/profile.d
    # TODO: install /etc/init.d/sailing start-up script
    sudo ln -s /home/sailing/code/configuration/sailing /etc/init.d/sailing
    sudo ln -s /home/sailing/code/configuration/sailing_server_setup/sailing.service /etc/systemd/system
    sudo systemctl daemon-reload
    sudo systemctl enable sailing.service
    # Configure SSH daemon:
    sudo cat << EOF >>/etc/ssh/sshd_config
PermitRootLogin without-password
PermitRootLogin Yes
MaxStartups 100
EOF
    # Increase limits
    sudo cat << EOF >>/etc/sysctl.conf
# number of connections the firewall can track
net.ipv4.ip_conntrac_max = 131072
EOF
    # Install mountnvmeswap stuff
    sudo ln -s /home/sailing/code/configuration/sailing_server_setup/mountnvmeswap /usr/local/bin
    sudo ln -s /home/sailing/code/configuration/sailing_server_setup/mountnvmeswap.service /etc/systemd/system
    sudo systemctl daemon-reload
    sudo systemctl enable mountnvmeswap.service
    # Install MongoDB 4.4 and configure as replica set "replica"
    sudo cat << EOF >/etc/yum.repos.d/mongodb-org.4.4.repo
[mongodb-org-4.4]
name=MongoDB Repository
baseurl=https://repo.mongodb.org/yum/amazon/2023/mongodb-org/4.4/x86_64/
gpgcheck=1
enabled=1
gpgkey=https://www.mongodb.org/static/pgp/server-4.4.asc
EOF
    sudo yum -y update
    sudo yum -y install mongodb-org-server mongodb-org-shell mongodb-org-tools
    sudo cat << EOF >>/etc/mongod.conf
replication:
  replSetName: replica
EOF
    sudo systemctl start mongod.service
    echo "rs.initiate()" | mongo
    # Install cron job for ssh key update for landscape managers
    sudo ln -s /home/sailing/code/configuration/update_authorized_keys_for_landscape_managers /usr/local/bin
    sudo ln -s /home/sailing/code/configuration/update_authorized_keys_for_landscape_managers_if_changed /usr/local/bin
    sudo ln -s /home/sailing/code/configuration/sailing_server_setup/crontab-root /root/crontab
    sudo su - -c "crontab /root/crontab"
    scp root@sapsailing.com:ssh-key-reader.token /tmp
    sudo mv /tmp/ssh-key-reader.token /root
    sudo chown root /root/ssh-key-reader.token
    sudo chgrp root /root/ssh-key-reader.token
    sudo chmod 600 /root/ssh-key-reader.token
  else
    echo "Not running on an AWS instance; refusing to run setup!" >&2
    exit 2
  fi
fi
