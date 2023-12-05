#!/bin/bash

# Purpose: Deployed in a file named "post-receive", in the hooks dir of a bare git repo. Upon push completion, this script runs and
# triggers an update to all instances' local repos (if the instance has a specific aws tag). A command is ran in the repo 
# after the merge completes. The user, in which the bare repo and hook are installed, must have aws credentials, that
# don't need mfa.

TAG="TESTPROXY"
DIR="/etc/httpd"
COMMAND="sudo service httpd reload"
# Gets all public ips for the instances with the chosen tag and iterates over the IPs.
for IP in $(aws ec2  describe-instances --filters Name=tag-key,Values="${TAG}" | jq -r '.Reservations[].Instances[].PublicIpAddress'); do
        # strictHostKey... means no authenticity check. The sync-repo... script must be installed in the root user's home.
        ssh -o "StrictHostKeyChecking=no" root@${IP} "cd ~ && ./sync-repo-and-execute-cmd.sh '${DIR}' '${COMMAND}'";
done;