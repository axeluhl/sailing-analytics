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
LOCAL_IPV4=$(ssh -o StrictHostKeyChecking=false root@"$1" "ec2-metadata --local-ipv4 2>/dev/null | sed \"s/local-ipv4: *//\"")
INSTANCE_ID=$(ssh -o StrictHostKeyChecking=false root@"$1" "ec2-metadata --instance-id 2>/dev/null | sed \"s/instance-id: *//\"")
if [[ -z "$LOCAL_IPV4" || -z "$INSTANCE_ID" ]]; then
    echo "Null local IP or Instance ID"
    exit 1
fi 
ELASTIC_IP="54.229.94.254"
INSTANCE_TAGS=("CentralReverseProxy" "ReverseProxy")
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
for tag in "${INSTANCE_TAGS[@]}"; do
    aws ec2 create-tags --resources "$INSTANCE_ID" --tags Key="$tag",Value=""
done
# The nlb is the exception case as we use the load balancer arn to further identify it.
nlbArn=$(aws elbv2 describe-tags --resource-arns $(echo "$target_groups" | jq -r '.TargetGroups | .[] | select(.LoadBalancerArns | .[] | contains("loadbalancer/net")  ) | .TargetGroupArn') | jq -r '.TagDescriptions | .[] | select(.Tags | any(.Key=="allReverseProxies") ) | .ResourceArn')
echo "Registering with nlb"
aws elbv2 register-targets --target-group-arn "$nlbArn" --targets Id="${LOCAL_IPV4}",Port=80
echo "Fetching tags"
# We fetch the tags of the target groups to identify those which the central reverse proxy should be added to.
# We depend on SAILING_TARGET_GROUP_NAME_PREFIX to filter out all target groups which point to sailing servers,
# because describe tags can take a maximum of 20 resource-arns.
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
HOSTED_ZONE_ID=$( aws route53 list-hosted-zones | \
           jq -r '.HostedZones[] | select(.Name == "sapsailing.com.").Id' | \
           sed -e 's|/hostedzone/||' )
read -n 1 -p "Check the instance has the correct tags and is in the correct target group.
Furthermore, check the batch file batch-for-route53-dns-record-update.json has been modified correctly.
Press a key to continue.." key_pressed
aws route53 change-resource-record-sets --hosted-zone-id ${HOSTED_ZONE_ID} --change-batch file://batch-for-route53-dns-record-update.json
# reload the nfs mountpoints.
echo "Waiting 60 seconds for records to change. The program will await a key press after this time."
echo "Please check the route53 DNS records have been correctly updated."
sleep 60
read -n 1 -p "Press a key to continue.." key_pressed
echo "Describing instances for remounting."
describe_instances=$(aws ec2 describe-instances)
echo "Sailing servers: "
for instanceIp in $(echo "$describe_instances"  | select_instances_by_tag  "sailing-analytics-server" | extract_public_ip); do
    echo "Remounting on $instanceIp"
    ssh -o StrictHostKeyChecking=false root@"${instanceIp}" "umount -l -f /home/scores;  mount -a"
done
echo "Disposable reverse proxies: "
for instanceIp in $(echo "$describe_instances"  | select_instances_by_tag  "DisposableProxy" | extract_public_ip); do
    echo "Remounting on $instanceIp"
    ssh -o StrictHostKeyChecking=false root@"${instanceIp}"  "umount -l -f /var/log/old; mount -a"
done
# Alter the elastic IP.
# WARNING: Will terminate connections via the existing public ip. 
aws ec2 associate-address --instance-id "${INSTANCE_ID}"  --public-ip "${ELASTIC_IP}"
git checkout main batch-for-route53-dns-record-update.json