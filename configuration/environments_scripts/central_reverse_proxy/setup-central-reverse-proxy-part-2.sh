#!/bin/bash

# PART 2
# Assumes already tagged, most files are already copied and that root contains $TEMPORARY_HOME_COPY_LOCATION folder which will be copied over to home.
IP=$1
IMAGE_TYPE="$2"
GIT_COPY_USER="wiki"
RELATIVE_PATH_TO_GIT="gitwiki" # the relative path to the repo within the git_copy_user
TEMPORARY_HOME_COPY_LOCATION="/root/temporary_home_copy" # home nested within this.
ssh -A "root@${IP}" "bash -s" << EOF
sudo systemctl start crond.service
. imageupgrade_functions.sh
cp -r --preserve "$TEMPORARY_HOME_COPY_LOCATION"/home /
rm -rf "$TEMPORARY_HOME_COPY_LOCATION"
build_crontab_and_setup_files -f "${IMAGE_TYPE}" "${GIT_COPY_USER}" "${RELATIVE_PATH_TO_GIT}"  # files have already been copied so -f is used.
chown trac:static /var/www/static
setup_keys "${IMAGE_TYPE}"
# setup nfs
systemctl enable nfs-server
echo "/var/log/old 172.31.0.0/16(rw,nohide,no_root_squash)
/home/scores 172.31.0.0/16(rw,nohide,no_root_squash)" >>/etc/exports
systemctl start nfs-server
cd /var/log/old/cache/docker/registry && docker-compose-up
internal_ip=\$(ec2-metadata --local-ipv4 | sed "s/local-ipv4: *//")
cd /root && sed -i "s/LOGFILES_INTERNAL_IP/\$internal_ip/" batch.json
cd /root && sed -i "s/SMTP_INTERNAL_IP/\$internal_ip/" batch.json
scp -o StrictHostKeyChecking=no -r root@sapsailing.com:/etc/ssh /etc
# append hostname to sysconfig
echo "HOSTNAME=sapsailing.com" >> /etc/sysconfig/network
sed -i "s/\(127.0.0.1 *\)/\1 sapsailing.com /" /etc/hosts
hostname sapsailing.com
hostnamectl set-hostname sapsailing.com
echo "Please logon and go to root where you will find 3 scripts. First authenticate with awsmfa (you may need to alter the bash alias for your credentials)."
echo "Then run /root/add-to-necessary-target-groups-and-setup-route53.sh"
echo "Then /root/remount-nfs-shares.sh because the previous script alters the logfiles and smtp entries"
echo "Finally run /root/set-elastic-ip.sh"
echo "Have a great day!"
EOF