#!/bin/bash
BASE_URL=""
BEARER_TOKEN=""

cd /etc/httpd/conf && git fetch
if [[ -n "$(git diff HEAD origin/main)" ]]
then
    echo "changing"
    cd /etc/httpd/conf && git merge origin/main #fastforward merge occurs
    sleep 2
    sudo service httpd reload
fi


#crontab */2 * * * * /<location/updateHttpd.sh