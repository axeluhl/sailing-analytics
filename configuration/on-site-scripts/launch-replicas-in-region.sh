#!/bin/bash
INSTANCE_TYPE=c4.2xlarge
REPLICA_SET_NAME=replica
REPLICA_SET_PRIMARY=localhost
KEY_NAME=Axel
VPC=Tokyo2020
COUNT=1

if [ $# -eq 0 ]; then
    echo "$0 -g <AWS-region> -R <release-name> -b <replication-bearer-token> [-c <instance-count>] [-r <replica-set-name>] [-p <host>:<port>] [-t <instance-type>] [-i <ami-id>] [-k <key-pair-name>] [-v <VPC name> ]"
    echo ""
    echo "-b replication bearer token; mandatory"
    echo "-c Count; defaults to ${COUNT}"
    echo "-i Amazon Machine Image (AMI) ID to use to launch the instance; defaults to latest image tagged with image-type:sailing-analytics-server"
    echo "-g AWS Region, e.g., eu-west-1"
    echo "-k Key pair name, mapping to the --key-name parameter"
    echo "-p Primary host:port; defaults to ${REPLICA_SET_PRIMARY}"
    echo "-r Replica set name; defaults to ${REPLICA_SET_NAME}"
    echo "-R release name; must be provided to select the release, e.g., build-202106040947"
    echo "-t Instance type; defaults to ${INSTANCE_TYPE}"
    echo "-v VPC name; defaults to ${VPC}"
    echo
    echo "Example: $0 -r archive -p dbserver.internal.sapsailing.com:10201 -P 0"
    exit 2
fi

options='g:R:b:c:r:p:P:t:a:i:k:v:'
while getopts $options option
do
    case $option in
	b) BEARER_TOKEN=$OPTARG;;
	c) COUNT=$OPTARG;;
	g) REGION=$OPTARG;;
        i) IMAGE_ID=$OPTARG;;
	k) KEY_NAME=$OPTARG;;
	p) REPLICA_SET_PRIMARY=$OPTARG;;
	R) RELEASE=$OPTARG;;
	r) REPLICA_SET_NAME=$OPTARG;;
        t) INSTANCE_TYPE=$OPTARG;;
	v) VPC=$OPTARG;;
        \?) echo "Invalid option"
            exit 4;;
    esac
done
export AWS_DEFAULT_REGION=$REGION
if [ -z $IMAGE_ID ]; then
  IMAGE_ID=$( `dirname $0`/../aws-automation/getLatestImageOfType.sh sailing-analytics-server )
fi
SECURITY_GROUP_ID=$( aws ec2 describe-security-groups --filters Name=tag:Name,Values="Sailing Analytics App" | jq -r '.SecurityGroups[].GroupId' )
echo "Found security group ${SECURITY_GROUP_ID} with name \"Sailing Analytics App\""
VPC_ID=$( aws --region ${REGION} ec2 describe-vpcs --filters Name=tag:Name,Values=${VPC} | jq -r '.Vpcs[].VpcId' )
echo "Found VPC ${VPC_ID}"
SUBNETS=$( aws --region ${REGION} ec2 describe-subnets --filters Name=vpc-id,Values=${VPC_ID} )
NUMBER_OF_SUBNETS=$( echo "${SUBNETS}" | jq -r '.Subnets | length' )
PRIVATE_IPS=""
i=0
while [ $i -lt $COUNT ]; do
  SUBNET_INDEX=$(( $RANDOM * $NUMBER_OF_SUBNETS / 32768 ))
  SUBNET_ID=$( echo "${SUBNETS}" | jq -r '.Subnets['${SUBNET_INDEX}'].SubnetId' )
  echo "Launching image with ID ${IMAGE_ID} into subnet #${SUBNET_INDEX} with ID ${SUBNET_ID} in VPC ${VPC_ID}"
  PRIVATE_IP=$( aws --region ${REGION} ec2 run-instances --subnet-id ${SUBNET_ID} --instance-type ${INSTANCE_TYPE} --security-group-ids ${SECURITY_GROUP_ID} --image-id ${IMAGE_ID} --user-data "INSTALL_FROM_RELEASE=${RELEASE}
SERVER_NAME=tokyo2020
MONGODB_URI=\"mongodb://${REPLICA_SET_PRIMARY}/tokyo2020-replica?replicaSet=${REPLICA_SET_NAME}&retryWrites=true&readPreference=nearest\"
USE_ENVIRONMENT=live-replica-server
REPLICATION_CHANNEL=tokyo2020-replica
REPLICATION_HOST=rabbit-ap-northeast-1.sapsailing.com
REPLICATE_MASTER_SERVLET_HOST=tokyo-ssh.internal.sapsailing.com
REPLICATE_MASTER_SERVLET_PORT=8888
REPLICATE_MASTER_EXCHANGE_NAME=tokyo2020
REPLICATE_MASTER_QUEUE_HOST=rabbit-ap-northeast-1.sapsailing.com
REPLICATE_MASTER_BEARER_TOKEN=${BEARER_TOKEN}
ADDITIONAL_JAVA_ARGS=\"${ADDITIONAL_JAVA_ARGS} -Dcom.sap.sse.debranding=true\"" --ebs-optimized --key-name $KEY_NAME --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=SL Tokyo2020 (Upgrade Replica)},{Key=sailing-analytics-server,Value=tokyo2020}]" "ResourceType=volume,Tags=[{Key=Name,Value=SL Tokyo2020 (Upgrade Replica)}]" | jq -r '.Instances[].PrivateIpAddress' )
  PRIVATE_IPS="${PRIVATE_IPS} ${PRIVATE_IP}"
  i=$(( i + 1 ))
done
# Now wait for those instances launched to become available
echo "Waiting until all hosts with their IPs${PRIVATE_IPS} are healthy..."
for PRIVATE_IP in ${PRIVATE_IPS}; do
  echo "Waiting for instance with private IP ${PRIVATE_IP} to become healthy..."
  while ! ssh -A -o StrictHostKeyChecking=no ec2-user@tokyo-ssh.sapsailing.com "ssh -o StrictHostKeyChecking=no sailing@${PRIVATE_IP} \"cd /home/sailing/servers/tokyo2020; ./status >/dev/null\""; do
    echo "${PRIVATE_IP} still not healthy. Trying again in 5s..."
    sleep 5
  done
done
