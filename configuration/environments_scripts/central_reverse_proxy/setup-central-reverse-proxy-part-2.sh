#!/bin/bash

# PART 2
# Assumes to be run after or be invoked by setup-central-reverse-proxy.sh which
# is assumed to have prepared content for user home folders in /root/temporary_home_copy,
# has set up the software packages, and that the user has mounted the volumes with their
# original content to /home, /var/log, et cetera.
#
# Call as follows:
#    setup-central-reverse-proxy-part-2.sh {external-ip-of-new-instance} {image-type}
# where {image-type} identifies the "environment" from configuration/environments_scripts
# which usually would be "central_reverse_proxy" here, passed in from the previous stage's
# script.
IP=$1
IMAGE_TYPE="$2"
GIT_COPY_USER="wiki"
RELATIVE_PATH_TO_GIT="gitwiki" # the relative path to the repo within the git_copy_user
TEMPORARY_HOME_COPY_LOCATION="/root/temporary_home_copy" # home nested within this.
ssh -A "root@${IP}" "bash -s" << EOF
sudo systemctl start crond.service
. imageupgrade_functions.sh
cp -r "$TEMPORARY_HOME_COPY_LOCATION"/home /
rm -rf "$TEMPORARY_HOME_COPY_LOCATION"
build_crontab_and_setup_files -f "${IMAGE_TYPE}" "${GIT_COPY_USER}" "${RELATIVE_PATH_TO_GIT}"  # files have already been copied so -f is used.
chown trac:static /var/www/static
# setup nfs
systemctl enable nfs-server
echo "/var/log/old 172.31.0.0/16(rw,nohide,no_root_squash)
/home/scores 172.31.0.0/16(rw,nohide,no_root_squash)" >>/etc/exports
systemctl start nfs-server
scp -p -o StrictHostKeyChecking=no -r root@sapsailing.com:/etc/ssh /etc
# append hostname to sysconfig
echo "HOSTNAME=sapsailing.com" >> /etc/sysconfig/network
sed -i "s/\(127.0.0.1 *\)/\1 sapsailing.com /" /etc/hosts
hostname sapsailing.com
hostnamectl set-hostname sapsailing.com
EOF
ssh -A -f root@"$IP" "cd /var/log/old/cache/docker/registry && nohup docker-compose up &>/dev/null &" &> /dev/null
echo "Please now run the script target-group-tag-route53-nfs-elasticIP-setup.sh which configures the EC2 instance tags, adds to the "
echo "necessary target groups, modifies a few records in route53 (logs.internal.sapsailing.com"
echo "and smtp.internal.sapsailing.com), remounts those dependent on this, and sets the elastic IP."
echo "You will need to have the aws cli installed and have the necessary permissions to make these alterations manually."
echo "In particular, make sure you have an active session token in your shell's environment, e.g., obtained"
echo "through the awsmfalogon.sh script."
echo "Have a great day!"
