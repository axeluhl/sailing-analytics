#!/bin/bash

selfIp=$( ec2-metadata --local-ipv4 | grep   -o "[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\>")
availabilityZone=$( ec2-metadata --availability-zone | grep -o "[a-zA-Z]\+-[a-zA-Z]\+-[0-9a-z]\+\>")
if [[ -z "$selfIp" || -z "$availabilityZone"  ]]; then
    echo "ec2-metadata not available" > /root/log
    selfIp=$(cat /var/cache/local-ip)
    availabilityZone=$(cat /var/cache/availability-zone)
fi
nlbName="HTTP-to-sapsailing-dot-com"
targetGroupArn=$(aws elbv2 describe-target-groups --name  "${nlbName}" | jq -r ".TargetGroups[].TargetGroupArn")
if [[ "$#" -eq 0 ]];then
    echo "Use add-to-nlb OR remove-from-nlb as the first and only argument."
    exit 2
fi

addSelfToNLB() {
    ec2-metadata --local-ipv4 | grep  -o "[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\>" > /var/cache/local-ip
    ec2-metadata --availability-zone |  grep -o "[a-zA-Z]\+-[a-zA-Z]\+-[0-9a-z]\+\>" > /var/cache/availability-zone
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
esac
