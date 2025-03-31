#!/usr/bin/env bash

# -----------------------------------------------------------
# Functions that are relevant for the configuration of load balancers.
# -----------------------------------------------------------

# -----------------------------------------------------------
# Create target group for load balancer
# @param $1  target group name
# @return    json result
# -----------------------------------------------------------
function create_target_group(){
	local_echo "Creating target group $1..."
	local target_group_name=$(only_letters_numbers_dash $1)
	local vpc_id=$(get_default_vpc_id)
	aws_wrapper elbv2 create-target-group --name $target_group_name --protocol "HTTPS" \
	--port "443" --vpc-id $vpc_id | get_attribute '.TargetGroups[0].TargetGroupArn'
}

# -----------------------------------------------------------
# Sets health check of target group
# @param $1  target group arn
# @param $2  health check protocol (e.g. "HTTP")
# @param $3  health check path (e.g. "/index.html")
# @param $4  health check port (e.g. "443")
# @param $5  health check interval seconds (e.g. "5")
# @param $6  health check timeouts seconds (e.g. "4")
# @param $7  healthy threshold count (e.g. "2")
# @param $8  unhealthy threshold count (e.g. "2")
# -----------------------------------------------------------
function set_target_group_health_check(){
	local_echo "Setting target group health check (protocol: $2, path: $3, port: $4, interval seconds: $5, timeout seconds: $6, unhealthy-count: $7, healthy-count: $8)..."
	aws_wrapper elbv2 modify-target-group --target-group-arn $1 --health-check-protocol $2 --health-check-path $3 --health-check-port $4\
	--health-check-interval-seconds $5 --health-check-timeout-seconds $6 --healthy-threshold-count $7 --unhealthy-threshold-count $8
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
	aws_wrapper elbv2 register-targets --target-group-arn $1 --targets Id=$2
}

# -----------------------------------------------------------
# Returns first https listener of load balancer
# @param $1  load balancer arn
# @return  https listener arn
# -----------------------------------------------------------
function get_first_https_listener(){
	aws_wrapper elbv2 describe-listeners --load-balancer-arn $1 --query "Listeners[?Protocol=='HTTPS'].ListenerArn" --output text
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

	# For testing purpose
	if [ $region == "eu-west-2" ]; then
		local domain="$subdomain.dummy2.sapsailing.com"
	else
		local domain="$subdomain.sapsailing.com"
	fi

	rule=$(aws_wrapper elbv2 create-rule --listener-arn $1 --priority $priority \
	--conditions Field=host-header,Values=$domain --actions Type=forward,TargetGroupArn=$3)
	echo $domain
}

# -----------------------------------------------------------
# Get rule with highest priority out of listener
# @param $1  listener arn
# @return    highest priority
# -----------------------------------------------------------
function get_rule_with_highest_priority(){
	local max_priority=$(aws --region=$region elbv2 describe-rules --listener-arn $1 --query "Rules[*].{P:Priority}" --output text | sanitize | sort -n | tail -1)
	if is_number $max_priority; then
		echo $max_priority
	else
		echo 0
	fi
}
