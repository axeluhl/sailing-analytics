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

	# Execute scenario "instance"
	instance_start

	header "Load balancer creation"

	local load_balancer_name=$(echo "$instance_name" | only_letters_and_numbers)
	local json_load_balancer=$(create_load_balancer_http "$load_balancer_name")
    #local json_load_balancer=$(create_load_balancer_https "$load_balancer_name" "$certificate_arn")

  local load_balancer_dns_name=$(echo "$json_load_balancer" | get_attribute '.DNSName')

	local json_health_check=$(configure_health_check_http "$load_balancer_name")
    # configure_health_check_https "$load_balancer_name"

	local json_added_instance_response=$(add_instance_to_elb "$load_balancer_name" "$instance_id")
	local added_instance_id=$(echo "$json_added_instance_response" | get_attribute '.Instances[0].InstanceId')

	header "Apache configuration"

	configure_apache "$load_balancer_dns_name" "$event_id" "$ssh_user" "$public_dns_name"

	echo "Finished."
}
