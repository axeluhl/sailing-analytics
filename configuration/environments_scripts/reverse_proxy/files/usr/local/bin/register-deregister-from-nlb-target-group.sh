#!/bin/bash

selfIp=$( ec2-metadata --local-ipv4 | grep   -o "[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\>")
availabilityZone=$( ec2-metadata --availability-zone | grep -o "[a-zA-Z]\+-[a-zA-Z]\+-[0-9a-z]\+\>")
LOG_LOCATION="/var/log/registration.err"
targetGroupTag="allReverseProxies"
if [[ -z "$selfIp" || -z "$availabilityZone"  ]]; then
    echo "ec2-metadata not available during nlb registration" > ${LOG_LOCATION}
    selfIp=$(cat /var/cache/local-ip)
    availabilityZone=$(cat /var/cache/availability-zone)
fi
nlbName="HTTP-to-sapsailing-dot-com"
# First we describe the target groups which are served by a load balancer, which has an ARN containing the substring "loadbalancer/net". Then, we get the tags of these target groups and iterate over the tag descriptions (a list of resourceArns paired with tags), selecting those resources which have a key "allReverseProxies". Finally we extract the resource ARN. Note: .[] means to iterate over an array.
targetGroupArn=$(aws elbv2 describe-tags --resource-arns $(aws elbv2 describe-target-groups | jq -r '.TargetGroups | .[] | select(.LoadBalancerArns | .[] | contains("loadbalancer/net")  ) | .TargetGroupArn') | jq -r '.TagDescriptions | .[] | select(.Tags | any(.Key=="allReverseProxies") ) | .ResourceArn')
if [[ "$#" -eq 0 ]];then
    echo "Use add-to-nlb OR remove-from-nlb as the first and only argument."
    exit 2
fi

addSelfToNLB() {
    user_data=$(ec2-metadata --user-data | sed -e 's/^user-data: //')
    if  ! echo "$user_data" | grep  "^image-upgrade$" && ! echo "$user_data" | grep "^no-startup-automation$" ; then
        ec2-metadata --local-ipv4 | grep  -o "[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\>" > /var/cache/local-ip
        ec2-metadata --availability-zone |  grep -o "[a-zA-Z]\+-[a-zA-Z]\+-[0-9a-z]\+\>" > /var/cache/availability-zone
        aws elbv2 register-targets --target-group-arn "${targetGroupArn}" --targets Id="${selfIp}",Port=80,AvailabilityZone="${availabilityZone}"
        # aws elbv2 register-targets --cli-input-json '{ "TargetGroupArn": "'"${targetGroupArn}"'","Targets":[{"Id": "'"${selfIp}"'","Port":80,"AvailabilityZone":"'"${availabilityZone}"'" }]}'  # using the complex json format
    fi 
}

removeSelfFromNLB() {
    aws elbv2 deregister-targets --target-group-arn "${targetGroupArn}" --targets Id="${selfIp}",Port=80,AvailabilityZone="${availabilityZone}"
}

for tagKey in $(aws elbv2 describe-tags --resource-arns "${targetGroupArn}"  | jq -r ".TagDescriptions | .[] | .Tags| .[] | .Key "); do
    if [[ "$tagKey" == "$targetGroupTag" ]]; then
        # assumes (correctly) that AWS target group keys are unique (ie. a target group cannot have the same key twice).
        case $1 in 
            add-to-nlb)
                addSelfToNLB
                ;;
            remove-from-nlb)
                removeSelfFromNLB
                echo "${selfIp} ${availabilityZone} ${targetGroupArn} $(date)" >>  ${LOG_LOCATION}
                ;;
        esac
    fi
done