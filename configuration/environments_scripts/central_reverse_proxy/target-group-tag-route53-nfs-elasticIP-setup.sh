#!/bin/bash
# This script is to be run after part 1 and part 2. It should be run locally and 
# requires the user to have the AWS CLI installed, as well as credentials. They must also have run the awsmfalogon.sh to authenticate.
# It will add the necessary tags, alter route 53 records, remount the nfs mounts that depend on these changes, add to the
# necessary target groups and switch the elastic IP. 
if [[ "$#" -ne 1 ]]; then
    echo "Please pass the remote IP address as the only parameter."
    echo "Please check comment description for usage."
    exit 2
fi
target_groups=$(aws elbv2 describe-target-groups)
LOCAL_IPV4=$(ssh root@"$1" "ec2-metadata --local-ipv4 | sed \"s/local-ipv4: *//\"")
INSTANCE_ID=$(ssh root@"$1" "ec2-metadata --instance-id | sed \"s/instance-id: *//\"")
ELASTIC_IP="54.229.94.254"
TAGS=("CentralReverseProxy" "ReverseProxy")
TARGET_GROUP_TAGS=("allReverseProxies" "CentralReverseProxy")
extract_public_ip() {
    jq -r ' .Instances | .[] | .PublicIpAddress' | grep -v null
}
select_instances_by_tag() {
    # $1: tag
    jq -r '.Reservations | .[] | select(.Instances | .[] | .Tags| any (.Key=="'"$1"'"))'
}
cd $(dirname "$0")
# give the instance the necessary tags.
for tag in "${TAGS[@]}"; do
    aws ec2 create-tags --resources "$INSTANCE_ID" --tags Key="$tag",Value=""
done
# The nlb is the exception case as we use the load balancer arn to further identify it.
nlbArn=$(aws elbv2 describe-tags --resource-arns $(echo "$target_groups" | jq -r '.TargetGroups | .[] | select(.LoadBalancerArns | .[] | contains("loadbalancer/net")  ) | .TargetGroupArn') | jq -r '.TagDescriptions | .[] | select(.Tags | any(.Key=="allReverseProxies") ) | .ResourceArn')
echo "Registering with nlb"
aws elbv2 register-targets --target-group-arn "$nlbArn"  --targets Id="${LOCAL_IPV4}",Port=80
echo "Fetching tags"
describe_tags=$(aws elbv2 describe-tags --resource-arns $(echo "$target_groups" | jq -r '.TargetGroups | .[] | select(.TargetGroupName | startswith("S-") | not ) | .TargetGroupArn'))
for tag in "${TARGET_GROUP_TAGS[@]}"; do
    echo "Adding to target groups with $tag"
    for tgArn in $(echo "$describe_tags" | jq -r '.TagDescriptions | .[] | select(.Tags | any(.Key=="'"$tag"'") ) | .ResourceArn'); do
        if [[ "$tgArn" != "$nlbArn" ]]; then
            echo "Registering in $tgArn as it has the correct tag"
            aws elbv2 register-targets --target-group-arn "$tgArn" --targets Id="${INSTANCE_ID}"
            [[ "$?" -eq 0 ]] || echo "Register target not successful"
        fi
    done
done
# alter records using batch file.
sed -i "s/LOGFILES_INTERNAL_IP/$LOCAL_IPV4/" batch-for-route53-dns-record-update.json
sed -i "s/SMTP_INTERNAL_IP/$LOCAL_IPV4/" batch-for-route53-dns-record-update.json
read -n 1  -p "Check the instance has the correct tags and is in the correct target group. Furthermore, check the batch file has been modified correctly. Press a key to continue.." key_pressed
###### DO NOT ENABLE WHILST TESTING: aws route53 change-resource-record-sets --hosted-zone-id Z2JYWXYWLLRLTE --change-batch file://batch-for-route53-dns-record-update.json
# reload the nfs mountpoints.
echo "Describing instances for remounting."
describe_instances=$(aws ec2 describe-instances)
for instanceIp in $(echo "$describe_instances"  | select_instances_by_tag  "sailing-analytics-server" | extract_public_ip); do
    echo "Remounting on $instanceIp"
    ssh root@"${instanceIp}"  " umount -l -f /home/scores;  mount -a"
done
for instanceIp in $(echo "$describe_instances"  | select_instances_by_tag  "DisposableProxy" | extract_public_ip); do
    echo "Remounting on $instanceIp"
    ssh root@"${instanceIp}"  "umount -l -f /var/log/old; mount -a"
done
# Alter the elastic IP.
# WARNING: Will terminate connections via the existing public ip. 
aws ec2 associate-address --instance-id "${INSTANCE_ID}"  --public-ip "${ELASTIC_IP}"
