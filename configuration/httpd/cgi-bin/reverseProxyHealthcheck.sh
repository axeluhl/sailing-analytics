#!/bin/bash

# Purpose: This script identifies whether the instance the script runs on is in the same AZ as the archive.
# The user it is installed on must have aws credentials that don't need mfa.


outputMessage() {
    #parameter 1: the message to display
    echo "Content-type: text/html" 
    echo ""
    echo $1
}


# The names of the variables in the macros file.
ARCHIVE_IP_NAME="ARCHIVE_IP"
ARCHIVE_FAILOVER_IP_NAME="ARCHIVE_FAILOVER_IP"
# The regex to extract the ip from a define var.
IP_REGEX="[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+$"
ARCHIVE_IP=$(grep -m 1 "^Define ${ARCHIVE_IP_NAME}\> .*" /etc/httpd/conf.d/000-macros.conf | grep -o "${IP_REGEX}")
ARCHIVE_FAILOVER=$(grep -m 1 "^Define ${ARCHIVE_FAILOVER_IP_NAME}\> .*" /etc/httpd/conf.d/000-macros.conf | grep -o "${IP_REGEX}")
store=$(curl -s --connect-timeout 4 "http://${ARCHIVE_IP}:8888/gwt/status")

if [[ 0 -eq $? ]]
then
	# check if main archive is in the same az by fetching the metadata
	MYAZ=$(curl -s  http://169.254.169.254/latest/meta-data/placement/availability-zone)
    # Fetch the instances in the region and then extract arrays, iterate through them, select those with the correct ip, and then gets the AZ.
	OTHERAZ=$(aws ec2 describe-instances | jq  --arg ip "$ARCHIVE_IP"   '..  | arrays | .[] | select(.PrivateIpAddress==$ip)' | jq '.. |  select(.AvailabilityZone?) | .AvailabilityZone')
	OTHERAZ=$(echo $OTHERAZ | grep -o "[a-z0-9-]*[^\"]")  #removes speech marks
	if [[ "$MYAZ" == "$OTHERAZ" ]]
	then
		echo "Status: 200 Healthy"
        outputMessage "Healthy: in the same az as the archive"
	else 
		echo "Status: 503 Not in the same az"
        outputMessage "Unhealthy: Not in the same az as the archive"
	fi
else
	# check if failover archive is in the same az
    echo "Status: 200 Healthy"
	outputMessage "failover at play: force healthy"
fi
