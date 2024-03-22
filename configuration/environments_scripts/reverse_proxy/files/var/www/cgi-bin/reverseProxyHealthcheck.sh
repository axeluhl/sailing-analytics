#!/bin/bash

# Purpose: A smart health check for a Apache httpd reverse proxy server that takes three
# things into account:
#  - /internal-server-status local technical health check
#  - the availability zone (AZ) of the current live ARCHIVE application server
#  - the health of all reverse proxies in a target group whose ARN is passed in the
#    QUERY_STRINGS environments variable (assuming this script runs as a CGI-BIN script)
#    as the "arn" query parameter, as in "...?arn=arn:aws:elasticloadbalancing:eu-west-1:017363970217:targetgroup/CentralWebServerHTTP-Dyn/32e57ea39e5fb165)
# The goal is to reduce cross-AZ traffic which produces extra cost and adds latency when
# a load balancer routes traffic to a reverse proxy in one AZ, and the application runs
# in a different AZ. So we'd like to have reverse proxies report an "unhealthy" status
# particularly if they run in an AZ different from the one in which the ARCHIVE application
# server runs, but only if there is at least one healthy reverse proxy in the ARCHIVE application
# server's AZ in the target group in question.
#
# To keep the health check swift, this script tries to keep the number of AWS API
# requests low. The first quick check is that for the local technical health, using the
# "/internal-server-status" endpoint. If that fails, this script will return an "unhealthy"
# status 500 and exit with code 1.
#
# When "/internal-server-status" reported a healthy status, the CIDRs of the VPC's subnets representing
# the AZs are cached persistently in a file once, using
#
#  aws ec2 describe-subnets | jq -r '.Subnets[].CidrBlock'
#
# This produces, e.g.,
#
#   172.31.16.0/20
#   172.31.32.0/20
#   172.31.0.0/20
#
# These CIDRs are not assumed to change during the life-cycle of this instance, so this needs
# to happen only one time. Using 
#
#     nmap -sL -n <net> | head -n -1 | sed -e '1d' | grep -q <IP>"
#
# we can check quickly whether an <IP> address is within a <net> CIDR.
#
# The ARCHIVE configuration with its production and failover instances can
# be determined from the /etc/httpd/conf.d/000-macros.conf file, telling the
# internal IP address of the ARCHIVE server currently used. From this, the AZ CIDR
# can be determined using the "nmap" approach described above.
#
# The same can be done for this instance's internal IP address, and since that is
# not assumed to change the local CIDR/subnet/AZ can also be cached persistently.
#
# If the local instance is in the same AZ as the current live ARCHIVE server,
# we'll report "healthy" because our traffic to the ARCHIVE will not be cross-AZ.
#
# Otherwise (different AZ than live ARCHIVE server), we need to find out if at
# least one reverse proxy that is in the live ARCHIVE server's AZ is healthy.
# To determine the other targets in the target group specified through the "arn="
# query parameter in the QUERY_STRING variable, an
#
#     aws elbv2 describe-target-health
#
# call is made, and the instance IDs returned are mapped to their internal IP addresses
# using an "aws ec2 describe-instances" call. This is the list of all reverse proxies
# registered with the target group at that time. Since this list is not considered to
# change very often, and because the negative effects of an outdated list are mild
# (in the worst case causing an instance in the wrong AZ to report "healthy", causing
# some temporary cross-AZ traffic), we cache the results and update this list only every
# few minutes and not upon every health check.
#
# From the list of targets we focus on those that are in the same AZ as the live ARCHIVE
# and check their health using the "/internal-server-status" endpoint. As soon as one
# healthy reverse proxy in the same AZ as the live ARCHIVE is found, this script will
# return an unhealthy 503 status and exit with code 2. If no healthy target in the live
# ARCHIVE's AZ is found, healthy (200) is reported and the script exits with code 0.
#
# (Keep in mind that with a load balancer that has cross-AZ balancing activated,
# requests may come in to a load balancer node in one AZ, and the only health reverse
# proxy lives in a different AZ; while this kind of cross-AZ traffic also will add
# latency, it doesn't add cost as it is considered "intra-loadbalancer traffic.")
#
# The user it is run by must have aws credentials that don't need mfa. Install to /usr/share/httpd/.aws.
#
# Exit status:
#  0 means all necessary checks could be performed and we're healthy (status 200)
#  1 means we were technically not healthy because the internal-server-status check failed (500)
#  2 means all checks could be performed, but we're not healthy (503) because not in the right AZ
#    and there is at least one healthy target in the correct AZ
#  3 means there was a problem determining our health; we may still report status 200 (healthy)

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

getAzCidr() {
    # $1: path to a file containing one CIDR per line
    # $2: an IP address to obtain the subnet CIDR for
    for cidr in $( cat "${1}" ); do
	if nmap -sL -n ${cidr} | head -n -1 | sed -e '1d' | grep -q ${2}; then
	    echo ${cidr}
	fi
    done
}

# The regex to extract the ip from a line ending in an ip.
IP_REGEX="[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+$"
MY_IP=$( ec2-metadata --local-ipv4 | grep -o "${IP_REGEX}")
# First check, using /internal-server-status, if we're technically healthy, and abort if not.
curl --silent --location --fail "http://${MY_IP}/internal-server-status" >/dev/null
if [[ "$?" -ne 0 ]]; then
    status "500 Reverse proxy itself is unhealthy"
    outputMessage "Unhealthy: Reverse proxy is unhealthy."
    exit 1
fi
# Create cache location if it doesn't exist:
CACHE_LOCATION="/var/cache/httpd/healthcheck" # Folder storing all the cached info, which the Apache user can access.
mkdir --parents ${CACHE_LOCATION}
if [[ "$1" == "cleanup" ]]; then
    rm -rf ${CACHE_LOCATION}
    status "200"
    outputMessage "cleanup complete"
    exit 0
fi
# Ensure we have a cached copy of the AZs' CIDRs:
ID_TO_AZ_FILENAME="${CACHE_LOCATION}/id_to_az"
AZ_CIDRS_FILENAME="${CACHE_LOCATION}/az_cidrs"
if [ ! -f "${AZ_CIDRS_FILENAME}" ]; then
  aws ec2 describe-subnets | jq -r '.Subnets[].CidrBlock' >"${AZ_CIDRS_FILENAME}"
fi
# The names of the variables in the macros file.
ARCHIVE_IP_NAME="ARCHIVE_IP"
ARCHIVE_FAILOVER_IP_NAME="ARCHIVE_FAILOVER_IP"
PRODUCTION_ARCHIVE_NAME="PRODUCTION_ARCHIVE"
MACROS_PATH="/etc/httpd/conf.d/000-macros.conf"
# Extracts which IP is in production.
PRODUCTION_ARCHIVE=$(sed -n -e  "s/^Define ${PRODUCTION_ARCHIVE_NAME}\> \(.*\)$/\1/p" ${MACROS_PATH})
# AZ of instance
MY_AZ_CIDR=$( getAzCidr "${AZ_CIDRS_FILENAME}" ${MY_IP} )
ARCHIVE_IP=$(grep -m 1 "^Define ${ARCHIVE_IP_NAME}\> .*"  ${MACROS_PATH} | grep -o "${IP_REGEX}")
ARCHIVE_FAILOVER_IP=$(grep -m 1 "^Define ${ARCHIVE_FAILOVER_IP_NAME}\> .*"  ${MACROS_PATH} | grep -o "${IP_REGEX}")
if [[ "$PRODUCTION_ARCHIVE" == "\${${ARCHIVE_IP_NAME}}" ]]; then
    PRODUCTION_ARCHIVE_IP=${ARCHIVE_IP}
else
    PRODUCTION_ARCHIVE_IP=${ARCHIVE_FAILOVER_IP}
fi
PRODUCTION_ARCHIVE_CIDR=$( getAzCidr "${AZ_CIDRS_FILENAME}" ${PRODUCTION_ARCHIVE_IP} )

# TODO remove debug output again
echo "PRODUCTION_ARCHIVE_IP: ${PRODUCTION_ARCHIVE_IP}; MY_AZ_CIDR: ${MY_AZ_CIDR}; PRODUCTION_ARCHIVE_CIDR: ${PRODUCTION_ARCHIVE_CIDR}"

# Check if in the same AZ as the live ARCHIVE server and report healthy in that case
if [ "${PRODUCTION_ARCHIVE_CIDR}" = "${MY_AZ_CIDR}" ]; then
    status "200"
    outputMessage "Healthy: In the same AZ as the archive."
    exit 0
else
    # Otherwise, get cached (or updated, if cache expired) list of reverse proxies in target group and
    # search for targets in same AZ as live ARCHIVE:
    TARGET_GROUP_ARN="${QUERY_STRING//arn=/}"
    TARGET_GROUP_NAME=$( basename $( dirname "${TARGET_GROUP_ARN}" ) )
    LAST_TARGET_GROUP_IPS="${CACHE_LOCATION}/last_target_ips_${TARGET_GROUP_NAME}"
    # Target group healthcheck timeout
    TARGET_GROUP_HEALTHCHECK_TIMEOUT_SECONDS=300
    if [[ ! -e "${LAST_TARGET_GROUP_IPS}" || "$(($current_time - $(stat --format '%Y' ${LAST_TARGET_GROUP_IPS}) ))" -gt "$(randomise $TARGET_GROUP_HEALTHCHECK_TIMEOUT_SECONDS)" ]]; then
	# This branch runs if the cached timestamp, of the last target group healthcheck, doesn't exist, or if it exceeds TARGET_GROUP_HEALTHCHECK_TIMEOUT_SECONDS.
	INSTANCE_IDS=$( aws elbv2 describe-target-health --target-group-arn ${TARGET_GROUP_ARN} | jq -r '.TargetHealthDescriptions[].Target.Id' )
	INSTANCE_PRIVATE_IPS=$( aws ec2 describe-instances --instance-ids $( echo "${INSTANCE_IDS}" | tr '\n' ' ' ) | jq -r '.Reservations[].Instances[].NetworkInterfaces[].PrivateIpAddress' )
	echo "${INSTANCE_PRIVATE_IPS}" >"${LAST_TARGET_GROUP_IPS}"
    else
	INSTANCE_PRIVATE_IPS=$( cat "${LAST_TARGET_GROUP_IPS}" )
    fi
    # TODO remove debug output again
    echo "INSTANCE_PRIVATE_IPS: ${INSTANCE_PRIVATE_IPS}"
    for INSTANCE_PRIVATE_IP in ${INSTANCE_PRIVATE_IPS}; do
	INSTANCE_CIDR=$( getAzCidr "${AZ_CIDRS_FILENAME}" $INSTANCE_PRIVATE_IP )
	# TODO remove debug output again
	echo "INSTANCE_CIDR: ${INSTANCE_CIDR}"
	if [ "${INSTANCE_CIDR}" = "${PRODUCTION_ARCHIVE_CIDR}" ]; then
	    # TODO remove debug output again
	    echo "Checking health of reverse proxy in production ARCHIVE's AZ: ${INSTANCE_PRIVATE_IP}"
	    # found a reverse proxy in the same AZ as the current live/production ARCHIVE; check its health:
	    curl --silent --location --fail "http://${INSTANCE_PRIVATE_IP}/internal-server-status" >/dev/null
	    if [[ "$?" = "0" ]]; then
		# the reverse proxy in the same AZ as the current live/production ARCHIVE is healthy; then we're not:
		status "503 Not in the same AZ"
		outputMessage "Unhealthy: Not in the same AZ as the archive; healthy instance in the same AZ as the archive."
		exit 2
	    fi
	fi
    done
    # No healthy reverse proxy found in the same AZ as live/production ARCHIVE, so we'll report healthy
    status "200"
    outputMessage "Healthy: No healthy instance in the same AZ; forcing healthy for ${TARGET_GROUP_HEALTHCHECK_TIMEOUT_SECONDS} seconds. (Result cached)"
    exit 0
fi
