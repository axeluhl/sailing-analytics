#!/bin/bash

# Purpose: This script is the internal server status.

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

SELF_IP=$( ec2-metadata --local-ipv4 | grep   -o "[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\>")
curl --silent --location --fail "http://${SELF_IP}/internal-server-status" >/dev/null
if [[ "$?" -eq 0 ]]; then
    status "200"
    outputMessage "failover at play: force healthy"
else
    status "500 Reverse proxy itself is unhealthy"
    outputMessage "Reverse proxy is unhealthy"
fi