#!/usr/bin/env bash

# -----------------------------------------------------------
# Create target group for load balancer
# @param $1  target group name
# @return    json result
# -----------------------------------------------------------
function create_target_group(){
	local_echo "Creating target group..."
	local target_group_name=$(alphanumeric $1)
	local vpc_id=$(get_default_vpc_id)
	aws_wrapper elbv2 create-target-group --name $target_group_name --protocol $target_group_protocol \
	--port $target_group_port --vpc-id $vpc_id | get_attribute '.TargetGroups[0].TargetGroupArn'
}

# -----------------------------------------------------------
# Sets health check of target group
# @param $1  target group arn
# @param $2  health check protocol (e.g. "HTTP")
# @param $3  health check path (e.g. "/index.html")
# @param $4  health check interval seconds (e.g. "5")
# @param $5  health check timeouts seconds (e.g. "4")
# @param $6  healthy threshold count (e.g. "2")
# @param $7  unhealthy threshold count (e.g. "2")
# -----------------------------------------------------------
function set_target_group_health_check(){
	aws_wrapper elbv2 modify-target-group --target-group-arn $1 --health-check-protocol $2 --health-check-path $3 \
	--health-check-interval-seconds $4 --health-check-timeout-seconds $5 --healthy-threshold-count $6 --unhealthy-threshold-count $7
}

# -----------------------------------------------------------
# Add tag to target group
# @param $1  target group arn
# @param $2  key (e.g. "Description")
# @param $2  value (e.g. "This is an instance for ...")
# -----------------------------------------------------------
function set_target_group_tag(){
	aws_wrapper elbv2 add-tags --resource-arns $1 --tags "Key=$2,Value=$3"
}

# -----------------------------------------------------------
# Register instances within target group
# @param $1  target group arn
# @param $2  instance id
# -----------------------------------------------------------
function register_targets(){
	local_echo "Register instance within target group..."
	aws elbv2 register-targets --target-group-arn $1 --targets Id=$2
}

# -----------------------------------------------------------
# Create rule for listener
# @param $1  listener arn
# @param $2  sub domain
# @param $3  target group arn
# @return    json result
# -----------------------------------------------------------
function create_rule(){
	local_echo "Creating rule for listener..."
	local subdomain=$(alphanumeric "$2")
	local priority=$(($(get_rule_with_highest_priority $1) + 1))
	local domain="$subdomain.sapsailing.com"
	aws_wrapper elbv2 create-rule --listener-arn $1 --priority $priority \
	--conditions Field=host-header,Values=$domain --actions Type=forward,TargetGroupArn=$3 1>&2
	echo $domain
}

# -----------------------------------------------------------
# Get rule with highest priority out of listener
# @param $1  listener arn
# @return    highest priority
# -----------------------------------------------------------
function get_rule_with_highest_priority(){
	local max_priority=$(aws_wrapper elbv2 describe-rules --listener-arn $1 \
	--query "sort_by(Rules, &Priority)[*].{P: Priority} | [-2:-1]" --output text)
	if is_number $max_priority; then
		echo $max_priority
	else
		echo 1
	fi
}

# NOT TESTED
# -----------------------------------------------------------
# Create load balancer with HTTPS rule
# @param $1  load_balancer_name
# @param $2  certificate arn
# @param $2  security group ids
# @return    json result
# -----------------------------------------------------------
function create_load_balancer_https(){
	local_echo "Creating load balancer \"$load_balancer_name\"..."
	local load_balancer_name=$(alphanumeric $1)
	aws_wrapper elb create-load-balancer --load-balancer-name $load_balancer_name \
	--listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" \
	"Protocol=HTTPS,LoadBalancerPort=443,InstanceProtocol=HTTP,InstancePort=80,SSLCertificateId=$2" \
	--availability-zones "$(get_availability_zones)" --security-groups "$elb_security_group_ids"
}

# -----------------------------------------------------------
# Configure health check HTTP
# @param $1  load balancer name
# @return    json result
# -----------------------------------------------------------
function configure_health_check_http(){
	local_echo "Configuring load balancer health check..."
	local load_balancer_name=$(alphanumeric $1)
	aws_wrapper elb configure-health-check --load-balancer-name $load_balancer_name \
	--health-check Target=HTTP:80,Interval=5,UnhealthyThreshold=2,HealthyThreshold=2,Timeout=4
}

# -----------------------------------------------------------
# Add instance to load balancer
# @param $1  load balancer name
# @param $2  instance id
# @return    json result
# -----------------------------------------------------------
function add_instance_to_clb(){
	local_echo "Adding instance to clb..."
	local load_balancer_name=$(alphanumeric $1)
	aws_wrapper elb register-instances-with-load-balancer --load-balancer-name $load_balancer_name --instances $2
}
