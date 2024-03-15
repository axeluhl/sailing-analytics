#!/bin/bash

# Purpose: This script identifies whether the instance the script runs on is in the same AZ as the archive.
# The user it is installed on must have aws credentials that don't need mfa. Install to  /usr/share/httpd/.aws.

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
# Cache locations
CACHE_LOCATION="/var/cache/httpd/healthcheck" # Folder storing all the cached info, which the Apache user can access.
ID_TO_AZ_FILENAME="${CACHE_LOCATION}/id_to_az"
ARCHIVE_IP_FILENAME="${CACHE_LOCATION}/archive_ip"
ARCHIVE_AZ_FILENAME="${CACHE_LOCATION}/archive_az"
mkdir --parents ${CACHE_LOCATION}
# IPs and AZs of instance
MY_IP=$( ec2-metadata --local-ipv4 | grep   -o "${IP_REGEX}")
MY_AZ=$( ec2-metadata --availability-zone | grep -o "${IP_REGEX}")
ARCHIVE_IP=$(grep -m 1 "^Define ${ARCHIVE_IP_NAME}\> .*"  ${MACROS_PATH} | grep -o "${IP_REGEX}")
ARCHIVE_TAG_KEY="temp-key"
ARCHIVE_TAG_VALUE="ARCHIVE"
if [[ "$1" == "cleanup" ]]; then
    rm -rf ${CACHE_LOCATION}
    status "200"
    outputMessage "cleanup complete"
    exit 0
fi
curl --silent --location --fail "http://${MY_IP}/internal-server-status" >/dev/null
if [[ "$?" -ne 0 ]]; then
    status "500 Reverse proxy itself is unhealthy"
    outputMessage "Reverse proxy is unhealthy"
    exit 0
fi
if [[ ! -e "$ID_TO_AZ_FILENAME" || ! -e "$ARCHIVE_AZ_FILENAME" || "$(cat ${ARCHIVE_IP_FILENAME})" != "${ARCHIVE_IP}" ]]; then
    # Cache instance IDs, archive AZ and the archive IP (as a sort of cache invalidator).
    instances=$(aws ec2 describe-instances)
    echo "$instances" | jq -r ".Reservations | .[] | .Instances | .[]" | jq -r '"\(.InstanceId) \(.Placement.AvailabilityZone)"' > ${ID_TO_AZ_FILENAME}
    tmp_az=$(echo "$instances" | jq -r '.Reservations | .[] | .Instances | .[] | select(.Tags | any(.Key=="'${ARCHIVE_TAG_KEY}'" and .Value=="'${ARCHIVE_TAG_VALUE}'")) | "\(.Placement.AvailabilityZone)"')
    if [[ -z "$tmp_az" ]]; then
        status "500 Unable to retrieve archive AZ"
        outputMessage "Cannot retrieve archive AZ, ensure the correct tags are in place"
        exit 1
    fi
    echo "$tmp_az" > ${ARCHIVE_AZ_FILENAME}
    echo "$ARCHIVE_IP" > $ARCHIVE_IP_FILENAME
fi
if [[ "$PRODUCTION_IP" == "\${${ARCHIVE_IP_NAME}}" ]]
then
    # Check if main archive is in the same AZ as the archive.
    archive_az=$(cat ${ARCHIVE_AZ_FILENAME})
    if [[ "$archive_az" == "$MY_AZ" ]]; then
        status "200"
        outputMessage "Healthy: in the same az as the archive"
    else
        # TODO: Perform check to see if there is anything healthy in the same AZ as the archive. Force healthy if no.
        aws elbv2 describe-target-health --target-group-arn ${QUERY_STRING//arn=/} >/dev/null
        status "503 Not in the same az"
        outputMessage "Unhealthy: Not in the same az as the archive"
    fi
else
    # We don't check if failover archive is in the same az
    status "200"
    outputMessage "failover at play: force healthy"
fi
