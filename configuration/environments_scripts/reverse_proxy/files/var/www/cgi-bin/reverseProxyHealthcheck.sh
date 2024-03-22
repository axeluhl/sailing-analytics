#!/bin/bash

# Purpose: This script identifies whether the instance the script runs on is in the same AZ as the archive, and is used as the healthcheck for the ALBs.
# It returns healthy if in the same AZ as the archive or if there is no other healthy instance in the same AZ. This is done to save costs by reducing cross-AZ
# traffic. Note, that all extra checks only occur if the internal-server-status is healthy.
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
randomise() {
    # $1: A number to slightly randomise
    echo $(($RANDOM % 20 + $1 ))
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
mkdir --parents ${CACHE_LOCATION}
ID_TO_AZ_FILENAME="${CACHE_LOCATION}/id_to_az"
ARCHIVE_IP_FILENAME="${CACHE_LOCATION}/archive_ip"
ARCHIVE_AZ_FILENAME="${CACHE_LOCATION}/archive_az"
LAST_TARGET_GROUP_HEALTHCHECK_RESULT_FILENAME="${CACHE_LOCATION}/last_target_group_healthcheck_result"
# The time it takes before the cache is reset.
CACHE_TIMEOUT_SECONDS=125
# Target group healthcheck timeout
TARGET_GROUP_HEALTHCHECK_TIMEOUT_SECONDS=30
# IPs and AZs of instance
MY_IP=$( ec2-metadata --local-ipv4 | grep -o "${IP_REGEX}")
MY_AZ=$( ec2-metadata --availability-zone | grep -o "[a-zA-Z]\+-[a-zA-Z]\+-[0-9a-z]\+\>")
ARCHIVE_IP=$(grep -m 1 "^Define ${ARCHIVE_IP_NAME}\> .*"  ${MACROS_PATH} | grep -o "${IP_REGEX}")
ARCHIVE_TAG_KEY="sailing-analytics-server"
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
    outputMessage "Unhealthy: Reverse proxy is unhealthy."
    exit 0
fi
current_time=$(date +"%s")
if [[ ! -e "$ID_TO_AZ_FILENAME" || ! -e "$ARCHIVE_AZ_FILENAME" || "$(cat ${ARCHIVE_IP_FILENAME})" != "${ARCHIVE_IP}" || "$(($current_time - $(stat --format "%Y" ${ARCHIVE_AZ_FILENAME}) ))" -gt "$(randomise $CACHE_TIMEOUT_SECONDS)" ]]; then
    # Cache instance IDs, archive AZ and the archive IP (as a sort of cache invalidator).
    instances=$(aws ec2 describe-instances)
    echo "$instances" | jq -r ".Reservations | .[] | .Instances | .[]" | jq -r '"\(.InstanceId) \(.Placement.AvailabilityZone)"' > ${ID_TO_AZ_FILENAME}
    tmp_az=$(echo "$instances" | jq -r '.Reservations | .[] | .Instances | .[] | select(.Tags | any(.Key=="'${ARCHIVE_TAG_KEY}'" and .Value=="'${ARCHIVE_TAG_VALUE}'")) | "\(.Placement.AvailabilityZone)"')
    if [[ -z "$tmp_az" ]]; then
        status "200"
        outputMessage "Healthy: Forced healthy, as retrieval of archive AZ failed. Ensure the correct tags are in place."
        exit 1
    fi
    echo "$tmp_az" > ${ARCHIVE_AZ_FILENAME}
    echo "$ARCHIVE_IP" > ${ARCHIVE_IP_FILENAME}
fi
if [[ "$PRODUCTION_IP" == "\${${ARCHIVE_IP_NAME}}" ]]; then
    archive_az=$(cat ${ARCHIVE_AZ_FILENAME})
    if [[ "$archive_az" == "$MY_AZ" ]]; then
        status "200"
        outputMessage "Healthy: In the same az as the archive."
    else
        if [[ ! -e "${LAST_TARGET_GROUP_HEALTHCHECK_RESULT_FILENAME}" || "$(($current_time - $(stat --format '%Y' ${LAST_TARGET_GROUP_HEALTHCHECK_RESULT_FILENAME}) ))" -gt "$(randomise $TARGET_GROUP_HEALTHCHECK_TIMEOUT_SECONDS)" ]]; then
            # This branch runs if the cached timestamp, of the last target group healthcheck, doesn't exist, or if it exceeds TARGET_GROUP_HEALTHCHECK_TIMEOUT_SECONDS.
            healthy_target_exists_in_same_az_as_archive="false"
            for instance_id in $(aws elbv2 describe-target-health --target-group-arn ${QUERY_STRING//arn=/} | jq -r '.TargetHealthDescriptions | .[] | select(.TargetHealth.State=="healthy") | .Target.Id'); do  ## TODO: Stop iterating if healthy instance is found.
                healthy_instance_az="$(sed -n "s/$instance_id \(.*\)/\1/p" $ID_TO_AZ_FILENAME)"
                if [[ "$healthy_instance_az" == "$(cat $ARCHIVE_AZ_FILENAME)" ]]; then
                    healthy_target_exists_in_same_az_as_archive="true"
                fi
            done
            if [[ "$healthy_target_exists_in_same_az_as_archive" == "true" ]]; then
                echo "unhealthy" > ${LAST_TARGET_GROUP_HEALTHCHECK_RESULT_FILENAME}
                status "503 Not in the same az"
                outputMessage "Unhealthy: Not in the same AZ as the archive; healthy instance in the same AZ as the archive. (Result cached)"
            else
                echo "healthy" > ${LAST_TARGET_GROUP_HEALTHCHECK_RESULT_FILENAME}
                status "200"
                outputMessage "Healthy: No healthy instance in the same AZ; forcing healthy for ${TARGET_GROUP_HEALTHCHECK_TIMEOUT_SECONDS} seconds. (Result cached)"
            fi
        else
            # This branch runs in the case that there is a timestamp file but the target group healthcheck has been performed within the TARGET_GROUP_HEALTHCHECK_TIMEOUT_SECONDS.
            if [[ -e "${LAST_TARGET_GROUP_HEALTHCHECK_RESULT_FILENAME}" && "$(cat $LAST_TARGET_GROUP_HEALTHCHECK_RESULT_FILENAME)" == "healthy" ]]; then
                status "200"
                outputMessage "Healthy: Using cached healthcheck value 'healthy' (despite residing in a different AZ to the ARCHIVE)."
            else
                status "503 Not in the same az"
                outputMessage "Unhealthy: Not in the same az as the archive and the value cached is unhealthy."
                rm ${LAST_TARGET_GROUP_HEALTHCHECK_RESULT_FILENAME}   # This ensures the unhealthy cache is shorter. We want the switchover to be quick, but if it stays healthy a little too long, that is not problematic.
            fi
        fi
    fi
else
    # We don't check if failover archive is in the same az
    status "200"
    outputMessage "Healthy: Failover at play, force healthy."
fi
