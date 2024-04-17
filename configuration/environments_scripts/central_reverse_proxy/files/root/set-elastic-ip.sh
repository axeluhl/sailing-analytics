#!/bin/bash
# WARNING: Will terminate connections via the existing public ip. 
LOCAL_IPV4=$(ec2-metadata --local-ipv4 | sed "s/local-ipv4: *//")
INSTANCE_ID=$(ec2-metadata --instance-id | sed "s/instance-id: *//")
ELASTIC_IP="54.229.94.254"
aws ec2 associate-address --instance-id "${INSTANCE_ID}"  --public-ip "${ELASTIC_IP}"