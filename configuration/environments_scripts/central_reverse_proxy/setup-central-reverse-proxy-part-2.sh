#!/bin/bash

# PART 2
# Assumes already tagged, most files are already copied and that root contains a /home folder which will be copied over.
IP=$1
BEARER_TOKEN=$2
IMAGE_TYPE="central_reverse_proxy"
HTTP_LOGROTATE_ABSOLUTE=/etc/logrotate.d/httpd
GIT_COPY_USER="trac"
RELATIVE_PATH_TO_GIT="gitcopy" # the relative path to the repo within the git_copy_user
ssh -A "root@${IP}" "bash -s" << EOF
sudo systemctl start crond.service
. imageupgrade_functions.sh
adduser --uid 1003 wiki
adduser --uid 1004 trac
usermod --append --groups trac wiki  # adds wiki to trac group.
adduser --uid 1014 --gid 1016 certbot
adduser --uid 1012 --gid 1013 scores
adduser --uid 1015 --gid 1017 httpdConf
build_crontab_and_setup_files -f "${IMAGE_TYPE}" "${GIT_COPY_USER}" "${RELATIVE_PATH_TO_GIT}"  # files have already been copied so -f is used.
usermod --append --groups trac wiki  # adds wiki to trac group.
# configure static ownership
groupadd --gid 1006 static
gpasswd -a trac static
gpasswd -a wiki static
chown trac:static /var/www/static
setup_keys "${IMAGE_TYPE}"
# setup nfs
systemctl enable nfs-server
echo "/var/log/old 172.31.0.0/16(rw,nohide,no_root_squash)
/home/scores 172.31.0.0/16(rw,nohide,no_root_squash)" >>/etc/exports
systemctl start nfs
cd /var/log/old/cache/docker/registry && docker-compose-up
internal_ip=\$(ec2-metadata --local-ipv4 | sed "s/local-ipv4: *//")
cd /root && sed -i "s/LOGFILES_INTERNAL_IP/\$internal_ip/" batch.json
cd /root && sed -i "s/SMTP_INTERNAL_IP/\$internal_ip/" batch.json
###### DO NOT ENABLE WHILST TESTING: aws route53 change-resource-record-sets --hosted-zone-id Z2JYWXYWLLRLTE --change-batch file://batch.json
cd /root && ./add-to-necessary-target-groups.sh
cd /root && ./remount-nfs-shares.sh
# scp -o StrictHostKeyChecking=no -r root@sapsailing.com:/etc/ssh /etc
# append hostname to sysconfig
echo "HOSTNAME=sapsailing.com" >> /etc/sysconfig/network
sed -i "s/\(127.0.0.1 *\)/\1 sapsailing.com /" /etc/hosts
hostname sapsailing.com
hostnamectl set-hostname sapsailing.com
# the setting of an elastic ip will terminate the connection
#### DO NOT ENABLE WHILST TESTING: cd /root && ./set-elastic-ip.sh
EOF