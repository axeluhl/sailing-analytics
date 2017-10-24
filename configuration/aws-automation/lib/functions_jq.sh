#!/usr/bin/env bash

# -----------------------------------------------------------
# Get subnet id of instance
# @param $1  json instance
# @return    subnet id
# -----------------------------------------------------------
function get_subnet_id(){
	echo $1 | jq -r '.Instances[0].SubnetId' | tr -d '\r'
}

# -----------------------------------------------------------
# Get instance id of instance
# @param $1  json instance
# @return    instance id
# -----------------------------------------------------------
function get_instance_id(){
	echo $1 | jq -r '.Instances[0].InstanceId' | tr -d '\r'
}

# -----------------------------------------------------------
# Get load balancer dns name
# @param $1  json load balancer
# @return    load balancer dns name
# -----------------------------------------------------------
function get_elb_dns_name(){
	echo $1 | jq -r '.DNSName' | tr -d '\r'
}

# -----------------------------------------------------------
# Get added instance id from json response of load balancer creation
# @param $1  json load balancer
# @return    added instance id
# -----------------------------------------------------------
function get_added_instance_from_elb(){
	echo "$1" | jq -r '.Instances[0].InstanceId'
}
