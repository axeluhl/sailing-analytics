#!/bin/bash
# Usage:
#   launch-mongodb-replica.sh <replica-set-name> <primary-host:primary-port> <priority>
#
# Example:
#   launch-mongodb-replica.sh live dbserver.internal.sapsailing.com:10202 1
#
aws ec2 run-instances --placement AvailabilityZone=eu-west-1c --instance-type i2.xlarge --security-group-ids sg-0a9bc2fb61f10a342 --image-id ami-0e7e1e80586782638 --count 1 --user-data "REPLICA_SET_NAME=$1
REPLICA_SET_PRIMARY=$2" --ebs-optimized --key-name Axel
