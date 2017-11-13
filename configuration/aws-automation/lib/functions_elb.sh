#!/usr/bin/env bash

# -----------------------------------------------------------
# Create load balancer with HTTP rule
# @param $1  load_balancer_name
# @return    json result
# -----------------------------------------------------------
function create_load_balancer_http(){
	local_echo "Creating load balancer \"$load_balancer_name\"..."
	aws_wrapper aws elb create-load-balancer --load-balancer-name $1 --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" --availability-zones "$(get_availability_zones)" --security-groups "$elb_security_group_ids"
}

# NOT TESTED
# -----------------------------------------------------------
# Create load balancer with HTTPS rule
# @param $1  load_balancer_name
# @return    json result
# -----------------------------------------------------------
function create_load_balancer_https(){
	local_echo 'Creating load balancer "$load_balancer_name"...'
	aws_wrapper aws elb create-load-balancer --load-balancer-name $1 --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" "Protocol=HTTPS,LoadBalancerPort=443,InstanceProtocol=HTTP,InstancePort=80,SSLCertificateId=$2" --availability-zones "$(get_availability_zones)" --security-groups "$elb_security_group_ids"
}

# -----------------------------------------------------------
# Configure health check HTTP
# @param $1  load balancer name
# @return    json result
# -----------------------------------------------------------
function configure_health_check_http(){
	local_echo "Configuring load balancer health check..."
	aws_wrapper aws elb configure-health-check --load-balancer-name "$1" --health-check Target=TCP:8888,Interval=15,UnhealthyThreshold=2,HealthyThreshold=3,Timeout=5
}

# NOT TESTED
# -----------------------------------------------------------
# Configure health check HTTPS
# @param $1  load balancer name
# @return    json result
# -----------------------------------------------------------
function configure_health_check_https(){
	local_echo "Configuring load balancer health check..."
	aws_wrapper aws elb configure-health-check --load-balancer-name "$1" --health-check Target=HTTPS:443/index.html,Interval=15,UnhealthyThreshold=2,HealthyThreshold=3,Timeout=5
}

# -----------------------------------------------------------
# Add instance to load balancer
# @param $1  load balancer name
# @param $2  instance id
# @return    json result
# -----------------------------------------------------------
function add_instance_to_elb(){
	local_echo "Adding instance to elb..."
	aws_wrapper aws elb register-instances-with-load-balancer --load-balancer-name $1 --instances $2
}
