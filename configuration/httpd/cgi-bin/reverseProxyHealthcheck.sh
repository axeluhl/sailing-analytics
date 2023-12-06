#!/bin/bash

# Purpose: This script identifies whether the instance the script runs on is in the same AZ as the archive.
# The user it is installed on must have aws credentials that don't need mfa.

outputMessage() {
    # parameter 1: the message to display on the site
    echo "Content-type: text/html" 
    echo ""
    echo $1
}

status() {
    # parameter 1: status code and messages
    echo "Status: $1"
}

# The names of the variables in the macros file.
ARCHIVE_IP_NAME="ARCHIVE_IP"
ARCHIVE_FAILOVER_IP_NAME="ARCHIVE_FAILOVER_IP"
PRODUCTION_IP_NAME="PRODUCTION_ARCHIVE"
MACROS_PATH="/etc/httpd/conf.d/000-macros.conf"
# The regex to extract the ip from a line ending in an ip.
IP_REGEX="[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+$"
# Extracts which IP is in production.
PRODUCTION_IP=$(sed -n -e  "s/^Define ${PRODUCTION_IP_NAME}\> \(.*\)$/\1/p" ${MACROS_PATH})
if [[ "$PRODUCTION_IP" == "\${${ARCHIVE_IP_NAME}}" ]]
then
	# Check if main archive is in the same az by fetching the metadata
	MYAZ=$(curl -s  http://169.254.169.254/latest/meta-data/placement/availability-zone)
    # Extract the actual IP of the archive
    ARCHIVE_IP=$(grep -m 1 "^Define ${ARCHIVE_IP_NAME}\> .*"  ${MACROS_PATH} | grep -o "${IP_REGEX}")
    # Fetch the instances in the region and then extract arrays, iterate through them, select those with the correct ip, and then gets the AZ.
	OTHERAZ=$(aws ec2 describe-instances | jq  --arg ip "$ARCHIVE_IP"   '..  | arrays | .[] | select(.PrivateIpAddress==$ip)' | jq '.. |  select(.AvailabilityZone?) | .AvailabilityZone')
	OTHERAZ=$(echo $OTHERAZ | grep -o "[a-z0-9-]*[^\"]")  # Removes speech marks
	if [[ "$MYAZ" == "$OTHERAZ" ]]
	then
		status "200"
        outputMessage "Healthy: in the same az as the archive"
	else 
		status "503 Not in the same az"
        outputMessage "Unhealthy: Not in the same az as the archive"
	fi
else
	# We don't check if failover archive is in the same az
    status "200"
	outputMessage "failover at play: force healthy"
fi
