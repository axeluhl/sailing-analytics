#!/bin/bash
extract_public_ip() {
    jq -r ' .Instances | .[] | .PublicIpAddress' | grep -v null
}
select_instances_by_tag() {
    # $1: tag
    jq -r '.Reservations | .[] | select(.Instances | .[] | .Tags| any (.Key=="'"$1"'"))'
}
echo "Describing instances"
describe_instances=$(aws ec2 describe-instances)
for instanceIp in $(echo "$describe_instances"  | select_instances_by_tag  "sailing-analytics-server" | extract_public_ip); do
    echo "Remounting on $instanceIp"
    ssh root@"${instanceIp}"  "umount -l -f /var/log/old; umount -l -f /home/scores;  mount -a"
done
for instanceIp in $(echo "$describe_instances"  | select_instances_by_tag  "DisposableProxy" | extract_public_ip); do
    echo "Remounting on $instanceIp"
    ssh root@"${instanceIp}"  "umount -l -f /var/log/old; mount -a"
done