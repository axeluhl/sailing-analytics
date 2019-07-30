#!/bin/bash
aws ec2 run-instances --placement AvailabilityZone=eu-west-1c --instance-type i2.xlarge --security-group-ids sg-0a9bc2fb61f10a342 --image-id ami-0e7e1e80586782638 --count 1 --user-data "REPLICA_SET_NAME=live
REPLICA_SET_PRIMARY=mongo0.internal.sapsailing.com:27017" --ebs-optimized --key-name Axel
