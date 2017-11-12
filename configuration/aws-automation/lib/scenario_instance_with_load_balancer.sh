#!/usr/bin/env bash

# Scenario for creating a route53 entry that is pointing
# to a load balancer which contains an instance
# ------------------------------------------------------

function instance_with_load_balancer_start(){
	instance_with_load_balancer_require
	instance_with_load_balancer_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function instance_with_load_balancer_require(){
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

function instance_with_load_balancer_execute() {
	header "Instance Initialization"

	local json_instance=$(run_instance)
	local instance_id=$(get_instance_id "$json_instance")

	wait_instance_exists "$instance_id"

	local public_dns_name=$(query_public_dns_name "$instance_id")

	header "SSH Connection"

	wait_for_ssh_connection "$key_file" "$ssh_user" "$public_dns_name"

	if $tail; then
		tail_start
	fi

	header "Event and user creation"

	wait_for_access_token_resource "$admin_username" "$admin_password" "$public_dns_name"
	local access_token=$(get_access_token "$admin_username" "$admin_password" "$public_dns_name")

	wait_for_create_event_resource "$public_dns_name"
	local event_id=$(create_event "$access_token" "$public_dns_name")

	change_admin_password "$access_token" "$public_dns_name" "$admin_username" "$new_admin_password"
	create_new_user "$access_token" "$public_dns_name" "$user_username" "$user_password"

    # ignore new user for the moment because updating its priviliges via rest is not yet implemented

	header "Load balancer creation"

	local load_balancer_name=$(echo "$instance_name" | trim)

	local json_load_balancer=$(create_load_balancer_http "$load_balancer_name")
    #local json_load_balancer=$(create_load_balancer_https "$load_balancer_name" "$certificate_arn")

    local load_balancer_dns_name=$(get_elb_dns_name "$json_load_balancer")

	local json_health_check=$(configure_health_check_http "$load_balancer_name")
    # configure_health_check_https "$load_balancer_name"

	local json_added_instance_response=$(add_instance_to_elb "$load_balancer_name" "$instance_id")
	local added_instance_id=$(get_added_instance_from_elb "$json_added_instance_response" )

	header "Route53 record creation"

	route53_change_resource_record "$load_balancer_name" 60 "$load_balancer_dns_name"

	header "Apache configuration"

	configure_apache "$load_balancer_dns_name" "$event_id" "$key_file" "$ssh_user" "$public_dns_name"

	echo "Finished."

	confirm_reset_panes
}
