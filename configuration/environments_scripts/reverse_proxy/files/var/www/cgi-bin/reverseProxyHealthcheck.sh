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

convert_ip_to_num() {
    # $1: Ip4 address in 4 octets form, separated by 3 dots.
    IFS=. read -r a b c d <<< "$1"
    echo "$(( ( $a << 24 ) + ( $b << 16 ) +  ($c << 8 ) + ($d) ))"
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
SUBNET_MASK_SIZE_VAR_LOCATION="/var/cache/httpd/subnetMaskSize"
if [[ "$1" == "cleanup" ]]; then
    rm -f $SUBNET_MASK_SIZE_VAR_LOCATION
    status "200"
    outputMessage "cleanup complete"
    exit 0
fi
SELF_IP=$( ec2-metadata --local-ipv4 | grep   -o "[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\>")
curl --silent --location --fail "http://${SELF_IP}/internal-server-status" >/dev/null
if [[ "$?" -ne 0 ]]; then
    status "500 Reverse proxy itself is unhealthy"
    outputMessage "Reverse proxy is unhealthy"
    exit 0
fi
if [[ ! -e "$SUBNET_MASK_SIZE_VAR_LOCATION" ]]; then
    # Get subnet mask size
    MY_AZ=$( ec2-metadata --availability-zone | grep -o "[a-zA-Z]\+-[a-zA-Z]\+-[0-9a-z]\+\>")
    aws ec2 describe-subnets | jq -r '.Subnets[] | select( .AvailabilityZone =="'"$MY_AZ"'")  | select (.Tags ==null or (.Tags | any(.Key == "noInstanceDeployment") | not )) | .CidrBlock | split("/") | .[1]' > $SUBNET_MASK_SIZE_VAR_LOCATION
fi
if [[ "$PRODUCTION_IP" == "\${${ARCHIVE_IP_NAME}}" ]]
then
    # Check if main archive is in the same az by comparing the network portion of the archive and this instance's IP, with the same bitmask.
    MY_IP=$( ec2-metadata --local-ipv4 | grep   -o "[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\>")
    MY_IP_VALUE="$(convert_ip_to_num "$MY_IP")"
    MY_AZ_MASK_SIZE=$(cat $SUBNET_MASK_SIZE_VAR_LOCATION) 
    # convert the value after the CIDR slash into a bitmask
    BITMASK_VALUE=0
    for i in $(seq $((31 - $MY_AZ_MASK_SIZE + 1)) 31); do
        BITMASK_VALUE=$(($BITMASK_VALUE | (1 << i ) ))
    done
    MY_SUBNET_VALUE=$(($BITMASK_VALUE & $MY_IP_VALUE))
    # Extract the actual IP of the archive
    ARCHIVE_IP=$(grep -m 1 "^Define ${ARCHIVE_IP_NAME}\> .*"  ${MACROS_PATH} | grep -o "${IP_REGEX}")
    ARCHIVE_IP_VALUE=$(convert_ip_to_num "$ARCHIVE_IP")
    ARCHIVE_SUBNET_VALUE=$(($BITMASK_VALUE & $ARCHIVE_IP_VALUE ))  #Apply the mask to the archive IP value.
    if [[ "$ARCHIVE_SUBNET_VALUE"  -eq  "$MY_SUBNET_VALUE" ]]; then 
        status "200"
        outputMessage "Healthy: in the same az as the archive"
    else 
        # TODO: Perform check to see if there is anything healthy in the same AZ as the archive. Force healthy if no.
        status "503 Not in the same az"
        outputMessage "Unhealthy: Not in the same az as the archive"
    fi
else
    # We don't check if failover archive is in the same az
    status "200"
    outputMessage "failover at play: force healthy"
fi
