#!/bin/bash
#This script can be used to indicate whether a reverse proxy should be healthy. We do this so any rev proxy in the same az as
#the primary archive is healthy. 
 
ARCHIVE_IP=$(grep -m 1 '^Define ARCHIVE_IP .*' /etc/httpd/conf.d/000-macros.conf | grep -o [0-9\.]*)
ARCHIVE_FAILOVER=$(grep -m 1 '^Define ARCHIVE_FAILOVER_IP .*' /etc/httpd/conf.d/000-macros.conf | grep -o [0-9\.]*)
store=$(curl -s http://${ARCHIVE_IP}:8888/gwt/status) #used to check if the main archive is in use
if [[ 0 -eq $? ]]
then
        #check if main archive is in the same az
        MYAZ=$(curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone)
        OTHERAZ=$(aws ec2 describe-instances | jq  --arg ip "$ARCHIVE_IP"   '..  | arrays | .[] | select(.PrivateIpAddress==$ip)' | jq '.. |  select(.AvailabilityZone?) | .AvailabilityZone')
        OTHERAZ=$(echo $OTHERAZ | grep -o '[a-z].*[^"]')
        if [[ "$MYAZ" == "$OTHERAZ" ]]
        then
                echo "healthy"
        else
                echo "unhealthy"
        fi
else
        #check if failover archive is in the same az
        echo "failover at play: force healthy"
fi
