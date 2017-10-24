#!/usr/bin/env bash

# -----------------------------------------------------------
# Create load balancer with HTTP rule
# @param $1  load_balancer_name
# @return    json result
# -----------------------------------------------------------
function create_load_balancer_http(){
	local_echo "Creating load balancer \"$load_balancer_name\"..."
	local json_result=$(create_load_balancer_http_command $1)

	if is_error $?; then
		error "Failed creating load balancer."
	else
		success "Created load balancer \"$load_balancer_name\"."
		echo $json_result
	fi
}

function create_load_balancer_http_command(){
	aws elb create-load-balancer --load-balancer-name $1 --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" --availability-zones "$(get_availability_zones)" --security-groups "$elb_security_group_ids"
}

# NOT TESTED
# -----------------------------------------------------------
# Create load balancer with HTTPS rule
# @param $1  load_balancer_name
# @return    json result
# -----------------------------------------------------------
function create_load_balancer_https(){
	local_echo 'Creating load balancer "$load_balancer_name"...'
	local json_result=$(create_load_balancer_https_command $1 $2)

	if is_error $?; then
		error "Failed creating load balancer."
	else
		success "Created load balancer \"$load_balancer_name\"."
		echo $json_result | tr -d '\r'
	fi
}

function create_load_balancer_https_command(){
	aws elb create-load-balancer --load-balancer-name $1 --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80" "Protocol=HTTPS,LoadBalancerPort=443,InstanceProtocol=HTTP,InstancePort=80,SSLCertificateId=$2" --availability-zones "$(get_availability_zones)" --security-groups "$elb_security_group_ids"
}

# -----------------------------------------------------------
# Configure health check HTTP
# @param $1  load balancer name
# @return    json result
# -----------------------------------------------------------
function configure_health_check_http(){
	local_echo "Configuring load balancer health check..."
	local json_result=$(configure_health_check_http_command $1)

	if is_error $?; then
		error "Failed configuring health check."
	else
		success "Successfully configured healthcheck for load balancer \"$load_balancer_name\"."
		echo $json_result
	fi
}

# $1: load_balancer_name
function configure_health_check_http_command(){
	aws elb configure-health-check --load-balancer-name "$1" --health-check Target=HTTP:80/index.html,Interval=15,UnhealthyThreshold=2,HealthyThreshold=3,Timeout=5
}

# NOT TESTED
# -----------------------------------------------------------
# Configure health check HTTPS
# @param $1  load balancer name
# @return    json result
# -----------------------------------------------------------
function configure_health_check_https(){
	local_echo "Configuring load balancer health check..."
	local json_result=$(configure_health_check_https $1)

	if is_error $?; then
		error "Failed configuring health check."
	else
		success "Successfully configured healthcheck for load balancer \"$load_balancer_name\"."
		echo $json_result
	fi
}

function configure_health_check_https_command(){
	aws elb configure-health-check --load-balancer-name "$1" --health-check Target=HTTPS:443/index.html,Interval=15,UnhealthyThreshold=2,HealthyThreshold=3,Timeout=5
}

# -----------------------------------------------------------
# Add instance to load balancer
# @param $1  load balancer name
# @param $2  instance id
# @return    json result
# -----------------------------------------------------------
function add_instance_to_elb(){
	local_echo "Adding instance to elb..."
	local json_result=$(add_instance_to_elb_command $1 $2)

	if is_error $?; then
		error "Failed adding instance to load balancer."
	else
		success "Successfully added instance \"$instance_id\" to load balancer \"$1\"."
		echo $json_result | tr -d '\r'
	fi
}

function add_instance_to_elb_command(){
	aws elb register-instances-with-load-balancer --load-balancer-name $1 --instances $2
}
