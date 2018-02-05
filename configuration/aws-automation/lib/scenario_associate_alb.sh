#!/usr/bin/env bash

# Add instance to application load balancer
# ------------------------------------------------------

function associate_alb_start(){
	associate_alb_require
	associate_alb_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function associate_alb_require(){
	require_region
	require_instance_type
	require_instance_name
	require_instance_short_name
	require_key_name
	require_key_file
	require_new_admin_password
	require_user_username
	require_user_password
}

function associate_alb_execute() {
	local vpc_id=$(get_default_vpc_id)
	local target_group_arn=$(create_target_group $instance_name $vpc_id | get_attribute '.TargetGroups[0].TargetGroupArn')
	register_targets $target_group_arn $instance_id
	local domain=$(create_rule $listener_arn $instance_short_name $target_group_arn)

	header "Apache configuration"
	configure_apache "$domain" "$event_id" "$ssh_user" "$public_dns_name" "ssl"
}
