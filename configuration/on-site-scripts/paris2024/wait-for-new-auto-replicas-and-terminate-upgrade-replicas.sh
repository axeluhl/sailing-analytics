#!/bin/bash
VPC="Paris2024"
TARGET_GROUP_NAME="S-paris2024"
UPGRADE_REPLICA_NAME="SL Paris2024 (Upgrade Replica)"
AUTO_REPLICA_NAME="SL Paris2024 (auto-replica)"

if [ $# -eq 0 ]; then
    echo "$0 [ -g <AWS-region> ]"
    echo ""
    echo "-g AWS Region, e.g., eu-west-1; if not provided, your default AWS region will be used"
    echo
    echo "Example: $0 -g ap-southeast-2"
    echo
    echo "Counts the upgrade replicas currently healthy in the ${TARGET_GROUP_NAME} target group"
    echo "and waits until as many auto-replicas are healthy in the same target group."
    echo "Then, all upgrade replicas are removed from the target group and then terminated."
    exit 2
fi

options='g:'
while getopts $options option
do
    case $option in
	g) REGION=$OPTARG;;
        \?) echo "Invalid option"
            exit 4;;
    esac
done
export AWS_DEFAULT_REGION=${REGION}
TARGET_GROUP_ARN=$( aws elbv2 describe-target-groups --names ${TARGET_GROUP_NAME} | jq -r '.TargetGroups[].TargetGroupArn' )
echo "Found target group with name ${TARGET_GROUP_NAME} and ARN ${TARGET_GROUP_ARN}"

UPGRADE_REPLICA_IDS=$( aws ec2 describe-instances --filters Name=tag:Name,Values="${UPGRADE_REPLICA_NAME}" | jq -r '.Reservations[].Instances[].InstanceId' )
NUMBER_OF_UPGRADE_REPLICAS=$( echo ${UPGRADE_REPLICA_IDS} | wc -w )
echo "Found ${NUMBER_OF_UPGRADE_REPLICAS} upgrade replicas in region ${REGION}. Waiting until we see this many auto-replicas named ${AUTO_REPLICA_NAME}..."
TOTAL_NUMBER_OF_TOTAL_REPLICAS_TO_WAIT_FOR=$(( 2 * ${NUMBER_OF_UPGRADE_REPLICAS} ))
NUMBER_OF_TOTAL_HEALTHY_REPLICAS=$( aws elbv2 describe-target-health --target-group-arn ${TARGET_GROUP_ARN} | jq '.TargetHealthDescriptions | map(select(.TargetHealth.State=="healthy")) | length' )
while [ ${NUMBER_OF_TOTAL_HEALTHY_REPLICAS} -lt ${TOTAL_NUMBER_OF_TOTAL_REPLICAS_TO_WAIT_FOR} ]; do
  echo "Found ${NUMBER_OF_TOTAL_HEALTHY_REPLICAS} healthy replicas in region ${REGION} so far; waiting until we see ${TOTAL_NUMBER_OF_TOTAL_REPLICAS_TO_WAIT_FOR}..."
  NUMBER_OF_TOTAL_HEALTHY_REPLICAS=$( aws elbv2 describe-target-health --target-group-arn ${TARGET_GROUP_ARN} | jq '.TargetHealthDescriptions | map(select(.TargetHealth.State=="healthy")) | length' )
  sleep 10
done
echo "Found a total of ${TOTAL_NUMBER_OF_TOTAL_REPLICAS_TO_WAIT_FOR} healthy replicas in region ${REGION} in target group ${TARGET_GROUP_NAME}. Terminating ${UPGRADE_REPLICA_NAME} replicas..."
for UPGRADE_REPLICA_ID in ${UPGRADE_REPLICA_IDS}; do
  echo "Terminating instance with ID ${UPGRADE_REPLICA_ID} in region ${REGION}..."
  aws ec2 terminate-instances --instance-ids ${UPGRADE_REPLICA_ID}
done
