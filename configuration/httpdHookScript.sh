#!/bin/bash

# Purpose: Deployed as "post-receive" in a hooks dir of a bare git repo, so that upon a push, all those instances with a cetain tag
# have a script triggered to update their local repos and then run another command within that repo. The user the hook
# is installed on, must have aws credentials that don't need mfa.

TAG="TESTPROXY"
DIR="/etc/httpd"
COMMAND="sudo service httpd reload"
# Gets all public ips for the instances with the chosen tag and iterates over the IPs.
for IP in $(aws ec2  describe-instances --filters Name=tag-key,Values="${TAG}" | jq -r '.Reservations[].Instances[].PublicIpAddress'); do
        # strictHostKey... means no authenticity check. The sync-repo... script must be installed in the root user's home.
        ssh -o "StrictHostKeyChecking=no" root@${IP} "cd ~ && ./sync-repo-and-execute-cmd.sh '${DIR}' '${COMMAND}'";
done;