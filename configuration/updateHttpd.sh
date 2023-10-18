#!/bin/bash
BASE_URL=""
BEARER_TOKEN=""
NEW=$(curl -s -H "Authorization: Bearer $BEARER_TOKEN" https://api.github.com/repos/$BASE_URL/commits | grep 'sha' | head -n1 | sed -r 's/"sha": "(.*)",/\1/' | tr -d '[:space:]')
CURRENT=$(cd /etc/httpd/conf && git rev-parse HEAD | tr -d '[:space:]')

if [[ $NEW != $CURRENT ]]
then
    echo "changing"
    cd /etc/httpd/conf && git pull git@github.com:$BASE_URL.git
    sleep 2
    sudo service httpd reload
fi

#crontab */2 * * * * /<location/updateHttpd.sh