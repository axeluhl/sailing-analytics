#!/usr/bin/env bash

# Create classic load balancer for instance
# ------------------------------------------------------

function associate_clb_start(){
	associate_clb_require
	associate_clb_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function associate_clb_require(){
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

function associate_clb_execute() {
	header "Associating classic load balancer"

  local load_balancer_dns_name=$(create_load_balancer_https "$instance_name" "$certificate_arn" | get_attribute '.DNSName')
  local json_health_check=$(configure_health_check_https "$instance_name")
	local json_added_instance_response=$(add_instance_to_elb "$instance_name" "$instance_id")

	header "Apache configuration"
	configure_apache "$load_balancer_dns_name" "$event_id" "$ssh_user" "$public_dns_name" "ssl"
}
