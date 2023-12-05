#!/bin/bash

# Purpose: Deployed as "post-receive" in a hooks dir of a bare git repo, so that upon a push, all those instances with a cetain tag
# have a script triggered to update their local repos and then run another command within that repo.

TAG="TESTPROXY"
DIR="/etc/httpd"
COMMAND="sudo service httpd reload"
for IP in $(aws ec2 --region eu-west-2 describe-instances --filters Name=tag-key,Values="${TAG}" | jq -r '.Reservations[].Instances[].PublicIpAddress'); do
        ssh -o "StrictHostKeyChecking=no" root@${IP} "cd ~ && ./sync-repo-and-execute-cmd.sh '${DIR}' '${COMMAND}'";
done;