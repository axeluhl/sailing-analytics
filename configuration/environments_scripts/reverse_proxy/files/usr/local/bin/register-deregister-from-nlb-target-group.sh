#!/bin/bash

selfIp=$( ec2-metadata --local-ipv4 | grep   -o "[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\>")
availabilityZone=$( ec2-metadata --availability-zone | grep -o "[a-zA-Z]\+-[a-zA-Z]\+-[0-9a-z]\+\>")
nlbName="HTTP-to-sapsailing-dot-com"
nlbName="IP-NAMES"
targetGroupArn=$(aws elbv2 describe-target-groups --name  "${nlbName}" | jq -r ".TargetGroups[].TargetGroupArn")
if [[ "$#" -eq 0 ]];then
    echo "Use add-to-nlb OR remove-from-nlb as the first and only argument."
fi

addSelfToNLB() {
    aws elbv2 register-targets --target-group-arn "${targetGroupArn}" --targets Id="${selfIp}",Port=80,AvailabilityZone="${availabilityZone}"
    # aws elbv2 register-targets --cli-input-json '{ "TargetGroupArn": "'"${targetGroupArn}"'","Targets":[{"Id": "'"${selfIp}"'","Port":80,"AvailabilityZone":"'"${availabilityZone}"'" }]}'  # using the complex json format
}


removeSelfFromNLB() {
        aws elbv2 deregister-targets --target-group-arn "${targetGroupArn}" --targets Id="${selfIp}",Port=80,AvailabilityZone="${availabilityZone}"

}



case $1 in 
    add-to-nlb)
        addSelfToNLB
        ;;
    remove-from-nlb)
        removeSelfFromNLB
        ;;