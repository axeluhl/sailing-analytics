#!/bin/bash
for instanceIp in $( aws ec2 describe-instances --filter Name=tag-key,Values=sailing-analytics-server | jq -r '.Reservations | .[] | .Instances | .[] | .PublicIpAddress' | grep -v null); do
    ssh root@"${instanceIp}"  "umount -l -f /var/log/old; umount -l -f /home/scores;  mount -a"
done
for instanceIp in $( aws ec2 describe-instances --filter Name=tag-key,Values=DisposableProxy | jq -r '.Reservations | .[] | .Instances | .[] | .PublicIpAddress' | grep -v null); do
    ssh root@"${instanceIp}"  "umount -l -f /var/log/old mount -a"
done