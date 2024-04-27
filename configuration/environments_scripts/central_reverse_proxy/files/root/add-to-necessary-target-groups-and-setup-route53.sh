#!/bin/bash
target_groups=$(aws elbv2 describe-target-groups)
LOCAL_IPV4=$(ec2-metadata --local-ipv4 | sed "s/local-ipv4: *//")
INSTANCE_ID=$(ec2-metadata --instance-id | sed "s/instance-id: *//")
nlbArn=$(aws elbv2 describe-tags --resource-arns $(echo "$target_groups" | jq -r '.TargetGroups | .[] | select(.LoadBalancerArns | .[] | contains("loadbalancer/net")  ) | .TargetGroupArn') | jq -r '.TagDescriptions | .[] | select(.Tags | any(.Key=="allReverseProxies") ) | .ResourceArn')
aws elbv2 register-targets --target-group-arn "$nlbArn"  --targets Id="${LOCAL_IPV4}",Port=80
describe_tags=$(aws elbv2 describe-tags --resource-arns $(echo "$target_groups" | jq -r '.TargetGroups | .[] | .TargetGroupArn'))
for tgArn in $(echo "$describe_tags" | jq -r '.TagDescriptions | .[] | select(.Tags | any(.Key=="allReverseProxies") ) | .ResourceArn'); do
    # adding to all groups tagged as reverse proxy (via allReverseProxies), except the NLB as it is registered as an IP target above.
    if [[ "$tgArn" != "$nlbArn" ]]; then
        aws elbv2 register-targets --target-group-arn "$tgArn" --targets Id="${INSTANCE_ID}"
    fi
done
for tgArn in $(echo "$describe_tags"  | jq -r '.TagDescriptions | .[] | select(.Tags | any(.Key=="CentralReverseProxy") ) | .ResourceArn'); do
    # adding to all groups tagged as central reverse proxy only (via CentralReverseProxy).
    aws elbv2 register-targets --target-group-arn "$tgArn" --targets Id="${INSTANCE_ID}"
done
###### DO NOT ENABLE WHILST TESTING: aws route53 change-resource-record-sets --hosted-zone-id Z2JYWXYWLLRLTE --change-batch file://batch.json
