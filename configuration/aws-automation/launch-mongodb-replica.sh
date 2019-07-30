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
IMAGE_ID=ami-0875074f93689aa3a
aws ec2 run-instances --placement AvailabilityZone=eu-west-1c --instance-type i2.xlarge --security-group-ids sg-0a9bc2fb61f10a342 --image-id $IMAGE_ID --count 1 --user-data "REPLICA_SET_NAME=$1
REPLICA_SET_PRIMARY=$2
REPLICA_SET_PRIORITY=$3" --ebs-optimized --key-name Axel
