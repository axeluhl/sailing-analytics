#!/bin/bash
BASE_URL=""
BEARER_TOKEN=""
CURRENT=$(cd /etc/httpd/conf && git rev-parse HEAD)
cd /etc/httpd/conf && git fetch
if [[ $CURRENT != $(git rev-parse origin/main) ]]
then
    echo "changing"
    cd /etc/httpd/conf && git merge origin/main #fastforward merge occurs
    sleep 2
    sudo service httpd reload
fi


#crontab */2 * * * * /<location/updateHttpd.sh