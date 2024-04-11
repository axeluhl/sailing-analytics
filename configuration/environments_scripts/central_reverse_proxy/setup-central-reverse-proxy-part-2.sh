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
EOF