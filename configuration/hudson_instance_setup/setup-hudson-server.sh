#!/bin/bash

# Usage: Launch an Amazon EC2 instance from an Amazon Linux 2 AMI with
# 100GB of root partition size and the "Sailing Analytics App" security group
# using an SSH key for which you have a working private key available.
# Then, run this script on your local computer, using the external IP address
# of the instance you just launched in AWS as only argument. This will then
# turn the instance into an application server for the SAP Sailing Analytics
# application. When the script is done you may log in to look around and check
# things. When done, shut down the instance (Stop, not Terminate) and create
# an image off of it, naming it, e.g., "SAP Sailing Analytics 2.0" and
# also tagging its root volume snapshot as, e.g., "SAP Sailing Analytics 2.0 (Root)".
# If you want to use the resulting image in production, also tag it with
# tag key "image-type" and tag value "sailing-analytics-server".
if [ $# != 0 ]; then
  SERVER=$1
  ../sailing_server_setup/setup-sailing-server.sh ${SERVER}
  scp "${0}" ec2-user@${SERVER}:
  ssh -A ec2-user@${SERVER} ./$( basename "${0}" )
else
  if ec2-metadata | grep -q instance-id; then
    echo "Running on an AWS EC2 instance as user ${USER} / $(whoami), starting setup..."
    # Install secrets
    scp root@sapsailing.com:dev-secrets /tmp
    scp root@sapsailing.com:hudson-aws-credentials /tmp
    sudo mv /tmp/dev-secrets /root/secrets
    mkdir /root/.aws
    sudo mv /tmp/hudson-aws-credentials /root/.aws/credentials
    sudo chown root:root /root/secrets /root/.aws/credentials
    sudo chmod 600 /root/secrets /root/.aws/credentials
    # Make eu-west-1 the default region for any aws CLI interaction:
    sudo su - -c "aws configure set default.region eu-west-1"
    # Create "hudson" user and clear its directory again which is to become a mount point
    sudo adduser hudson
    sudo su - hudson -c "rm -rf /home/hudson/* /home/hudson/.* 2>/dev/null"
    sudo mkdir /usr/lib/hudson
    sudo chown hudson /usr/lib/hudson
    sudo mkdir /var/log/hudson
    sudo chgrp hudson /var/log/hudson
    sudo chmod g+w /var/log/hudson
    sudo wget -O /usr/lib/hudson/hudson.war "https://static.sapsailing.com/hudson.war.patched-with-mail-1.6.2"
    # Link hudson file to /etc/init.d
    sudo ln -s /home/sailing/code/configuration/hudson_instance_setup/hudson /etc/init.d
    # Link hudson service to /etc/systemd/system
    sudo ln -s /home/sailing/code/configuration/hudson_instance_setup/hudson.service /etc/systemd/system
    # Link Hudson system-wide config file:
    sudo ln -s /home/sailing/code/configuration/hudson_instance_setup/sysconfig-hudson /etc/sysconfig/hudson
    # Link additional script files needed for Hudson build server control:
    sudo ln -s /home/sailing/code/configuration/launchhudsonslave /usr/local/bin
    sudo ln -s /home/sailing/code/configuration/launchhudsonslave-java11 /usr/local/bin
    sudo ln -s /home/sailing/code/configuration/aws-automation/getLatestImageOfType.sh /usr/local/bin
    # Enable the service:
    sudo systemctl daemon-reload
    sudo systemctl enable hudson.service
    # NFS-export Android SDK
    sudo su - -c "cat <<EOF >>/etc/exports
/home/hudson/android-sdk-linux 172.31.0.0/16(rw,nohide,no_root_squash)
EOF
"
    # Allow "hudson" user to launch EC2 instances:
    sudo su - -c "cat <<EOF >>/etc/sudoers.d/hudsoncanlaunchec2instances
hudson             ALL = (root) NOPASSWD: /usr/local/bin/launchhudsonslave
hudson             ALL = (root) NOPASSWD: /usr/local/bin/launchhudsonslave-java11
hudson             ALL = (root) NOPASSWD: /usr/local/bin/getLatestImageOfType.sh
EOF
"
    # Install DEV server
    sudo su - sailing -c "mkdir /home/sailing/servers/DEV
cd /home/sailing/servers/DEV
cat <<EOF | /home/sailing/code/java/target/refreshInstance.sh auto-install-from-stdin
USE_ENVIRONMENT=dev-server
EOF
"
    sudo cp /root/secrets /home/sailing/servers/DEV/configuration
    sudo chown sailing /home/sailing/servers/DEV/configuration/secrets
    sudo chgrp sailing /home/sailing/servers/DEV/configuration/secrets
    sudo cp /root/mail.properties /home/sailing/servers/DEV/configuration
    sudo chown sailing /home/sailing/servers/DEV/configuration/mail.properties
    sudo chgrp sailing /home/sailing/servers/DEV/configuration/mail.properties
    # Start the sailing.service with empty/no user data, so the next boot is recognized as a re-boot
    sudo systemctl start sailing.service
    sudo systemctl stop sailing.service
    sudo mount -a
  else
    echo "Not running on an AWS instance; refusing to run setup!" >&2
    echo "To prepare an instance running in AWS, provide its external IP as argument to this script." >&2
    exit 2
  fi
fi
