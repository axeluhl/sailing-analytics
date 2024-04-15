#!/bin/bash

#PART 2
IP=$1
BEARER_TOKEN=$2
IMAGE_TYPE="central_reverse_proxy"
HTTP_LOGROTATE_ABSOLUTE=/etc/logrotate.d/httpd
GIT_COPY_USER="trac"
RELATIVE_PATH_TO_GIT="gitcopy" # the relative path to the repo within the git_copy_user
ssh -A "root@${IP}" "bash -s" << EOF

. imageupgrade_functions.sh
build_crontab_and_setup_files -f "${IMAGE_TYPE}" "${GIT_COPY_USER}" "${RELATIVE_PATH_TO_GIT}"  # files have already been copied so -f is used.
sudo systemctl start crond.service
# append hostname to sysconfig
echo "HOSTNAME=sapsailing.com" >> /etc/sysconfig/network
sed -i "s/\(127.0.0.1 *\)/\1 sapsailing.com /" /etc/hosts
hostname sapsailing.com
hostnamectl set-hostname sapsailing.com
setup_keys "${IMAGE_TYPE}"
scp -r root@sapsailing.com:/etc/ssh /etc
EOF