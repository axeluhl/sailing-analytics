#!/bin/bash
# Usage:
#   launch-mongodb-replica.sh [ <replica-set-name> [ <primary-host:primary-port> [ <priority> ] ] ]
#
# The defaults are: live, mongo0.internal.sapsailing.com:27017, 1
#
# Examples:
#   launch-mongodb-replica.sh
#        connects to the "live" replica set through mongo0.internal.sapsailing.com:27017 with priority 1
#   launch-mongodb-replica.sh archive dbserver.internal.sapsailing.com:10201 0
#        connects to the "archive" replica set at dbserver.internal.sapsailing.com:10201 and ensures
#        that the new replica never becomes PRIMARY by setting the priority to 0
#
AVAILABILITY_ZONE=eu-west-1c
INSTANCE_TYPE=i2.xlarge
IMAGE_ID=ami-01b868df7b51fa5e5
KEY_NAME=Axel
REPLICA_SET_NAME=
REPLICA_SET_PRIMARY=
REPLICA_SET_PRIORITY=
REPLICA_SET_VOTES=

if [ $# -eq 0 ]; then
    echo "$0 [-r <replica-set-name>] [-p <host>:<port>] [-P <priority] [-t <instance-type>] [-a <availability-zone>] [-i <ami-id>] [-k <key-pair-name>] [-v <votes>]"
    echo ""
    echo "-r Replica set name, e.g., live"
    echo "-p Primary host:port, e.g., mongo0.internal.sapsailing.com:27017"
    echo "-P priority, e.g., 0 if the replica shall never become PRIMARY"
    echo "-v number of votes, e.g., 0 to not let this affect the PRIMARY's votes"
    echo "-t Instance type; defaults to $INSTANCE_TYPE"
    echo "-a Availability zone, defaults to $AVAILABILITY_ZONE"
    echo "-i Amazon Machine Image (AMI) ID to use to launch the instance"
    echo "-k Key pair name, mapping to the --key-name parameter"
    echo ""
    echo "Launches an instance, patches the mongod.conf to accomodate for the replica set name and"
    echo "launches the MongoDB server on 0.0.0.0:27017. The server is then added as a replica set"
    echo "to the server configured via the -p parameter, defaulting to mongo0.internal.sapsailing.com:27017."
    echo "When the server shuts down, the replica is removed from the replica set automatically."
    echo
    echo "Example: $0 -r archive -p dbserver.internal.sapsailing.com:10201 -P 0"
    exit 2
fi

options='r:p:P:t:a:i:k:'
while getopts $options option
do
    case $option in
	r) REPLICA_SET_NAME=$OPTARG;;
	p) REPLICA_SET_PRIMARY=$OPTARG;;
	P) REPLICA_SET_PRIORITY=$OPTARG;;
	v) REPLICA_SET_VOTES=$OPTARG;;
        t) INSTANCE_TYPE=$OPTARG;;
        a) AVAILABILITY_ZONE=$OPTARG;;
        i) IMAGE_ID=$OPTARG;;
	k) KEY_NAME=$OPTARG;;
        \?) echo "Invalid option"
            exit 4;;
    esac
done

aws ec2 run-instances --placement AvailabilityZone=$AVAILABILITY_ZONE --instance-type $INSTANCE_TYPE --security-group-ids sg-0a9bc2fb61f10a342 --image-id $IMAGE_ID --count 1 --user-data "REPLICA_SET_NAME=$REPLICA_SET_NAME
REPLICA_SET_PRIMARY=$REPLICA_SET_PRIMARY
REPLICA_SET_PRIORITY=$REPLICA_SET_PRIORITY
REPLICA_SET_VOTES=$REPLICA_SET_VOTES" --ebs-optimized --key-name $KEY_NAME --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=MongoDB Replica Set $REPLICA_SET_NAME P$REPLICA_SET_PRIORITY}]" "ResourceType=volume,Tags=[{Key=Name,Value=MongoDB Replica Set $REPLICA_SET_NAME P$REPLICA_SET_PRIORITY}]"
