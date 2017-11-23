#!/usr/bin/env bash

# Scenario for creating a route53 entry that is pointing
# to an elastic ip which points to an instance
# ------------------------------------------------------

function instance_with_alb_start(){
	instance_with_alb_require
	instance_with_alb_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function instance_with_alb_require(){
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

function instance_with_alb_execute() {
	# Execute scenario "instance"
	instance_start

	local vpc_id=$(get_default_vpc_id)
	local target_group_arn=$(create_target_group $instance_name $vpc_id | get_attribute '.TargetGroups[0].TargetGroupArn')
	register_targets $target_group_arn $instance_id
	local rule=$(create_rule $listener_arn $instance_short_name $target_group_arn)

}
