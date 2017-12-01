#!/usr/bin/env bash


# -----------------------------------------------------------
# Create target group for load balancer
# @param $1  target group name
# @return    json result
# -----------------------------------------------------------
function create_target_group(){
	local_echo "Creating target group..."
	local target_group_name=$(echo "$1" | only_letters_and_numbers)
	aws_wrapper elbv2 create-target-group --name $target_group_name --protocol HTTPS --port 443 --vpc-id "$2"
}

# -----------------------------------------------------------
# Register instances within target group
# @param $1  target group arn
# @param $2  instance id
# -----------------------------------------------------------
function register_targets(){
	local_echo "Register instance within target group..."
	aws elbv2 register-targets --target-group-arn "$1" --targets Id="$2"
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
	local subdomain=$(echo "$2" | only_letters_and_numbers)
	local priority=$(($(get_rule_with_highest_priority $1) + 1))
	local domain="$subdomain.dummy.sapsailing.com"
	aws_wrapper elbv2 create-rule --listener-arn $1 --priority $priority --conditions Field=host-header,Values="$domain" --actions Type=forward,TargetGroupArn=$3 1>&2
	echo "$domain"
}

# -----------------------------------------------------------
# Get rule with highest priority out of listener
# @param $1  listener arn
# @return    highest priority
# -----------------------------------------------------------
function get_rule_with_highest_priority(){
	local max_priority=$(aws_wrapper elbv2 describe-rules --listener-arn $1 --query "sort_by(Rules, &Priority)[*].{P: Priority} | [-2:-1]" --output text)
	if is_number $max_priority; then
		echo $max_priority
	else
		echo 1
	fi
}

# -----------------------------------------------------------
# Create load balancer with HTTP rule
# @param $1  load_balancer_name
# @return    json result
# -----------------------------------------------------------
function create_load_balancer_http(){
	local load_balancer_name=$(echo "$1" | only_letters_and_numbers)
	local_echo "Creating load balancer \"$load_balancer_name\"..."
	aws_wrapper elb create-load-balancer --load-balancer-name $load_balancer_name --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" --availability-zones "$(get_availability_zones)" --security-groups "$elb_security_group_ids"
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
	local load_balancer_name=$(echo "$1" | only_letters_and_numbers)
	local_echo "Creating load balancer \"$load_balancer_name\"..."
	aws_wrapper elb create-load-balancer --load-balancer-name $load_balancer_name --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" "Protocol=HTTPS,LoadBalancerPort=443,InstanceProtocol=HTTP,InstancePort=80,SSLCertificateId=$2" --availability-zones "$(get_availability_zones)" --security-groups "$elb_security_group_ids"
}

# -----------------------------------------------------------
# Configure health check HTTP
# @param $1  load balancer name
# @return    json result
# -----------------------------------------------------------
function configure_health_check_http(){
	local load_balancer_name=$(echo "$1" | only_letters_and_numbers)
	local_echo "Configuring load balancer health check..."
	aws_wrapper elb configure-health-check --load-balancer-name "$load_balancer_name" --health-check Target=TCP:8888,Interval=15,UnhealthyThreshold=2,HealthyThreshold=3,Timeout=5
}

# NOT TESTED
# -----------------------------------------------------------
# Configure health check HTTPS
# @param $1  load balancer name
# @return    json result
# -----------------------------------------------------------
function configure_health_check_https(){
	local_echo "Configuring load balancer health check..."
	local load_balancer_name=$(echo "$1" | only_letters_and_numbers)
	aws_wrapper elb configure-health-check --load-balancer-name "$load_balancer_name" --health-check Target=HTTPS:443/index.html,Interval=15,UnhealthyThreshold=2,HealthyThreshold=3,Timeout=5
}

# -----------------------------------------------------------
# Add instance to load balancer
# @param $1  load balancer name
# @param $2  instance id
# @return    json result
# -----------------------------------------------------------
function add_instance_to_elb(){
	local_echo "Adding instance to elb..."
	local load_balancer_name=$(echo "$1" | only_letters_and_numbers)
	aws_wrapper elb register-instances-with-load-balancer --load-balancer-name $load_balancer_name --instances $2
}
