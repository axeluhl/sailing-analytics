#!/usr/bin/env bash

# Scenario for creating a route53 entry that is pointing
# to an elastic ip which points to an instance
# ------------------------------------------------------

function instance_with_elastic_ip_start(){
	instance_with_elastic_ip_require
	instance_with_elastic_ip_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function instance_with_elastic_ip_require(){
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

function instance_with_elastic_ip_execute() {
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

	header "Elastic ip creation"

	local elastic_ip=$(allocate_address)
	associate_address "$instance_id" "$elastic_ip"

	header "Route53 record creation"

	local subdomain_name=$(echo "$instance_name" | trim)
	route53_change_resource_record "$subdomain_name" 60 "$elastic_ip"

	header "Apache configuration"

	configure_apache "$elastic_ip" "$event_id" "$key_file" "$ssh_user" "$public_dns_name"

	echo "Finished."

	confirm_reset_panes
}
