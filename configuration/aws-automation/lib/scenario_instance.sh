#!/usr/bin/env bash

# Scenario for creating an instance
# ------------------------------------------------------

function instance_start(){
	instance_require
	instance_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function instance_require(){
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

function instance_execute() {
	header "Instance Initialization"

	local json_instance=$(run_instance)
	instance_id=$(echo "$json_instance" | get_attribute '.Instances[0].InstanceId')
  private_ip=$(echo "$json_instance" | get_attribute '.Instances[0].PrivateIpAddress')

	wait_instance_exists "$instance_id"

	public_dns_name=$(query_public_dns_name "$instance_id")

	header "SSH Connection"

	wait_for_ssh_connection "$ssh_user" "$public_dns_name"

	if $tail; then
		tail_start
	fi

	header "Event and user creation"

	wait_for_access_token_resource "$admin_username" "$admin_password" "$public_dns_name"
	access_token=$(get_access_token "$admin_username" "$admin_password" "$public_dns_name")

	wait_for_create_event_resource "$public_dns_name"
	event_id=$(create_event "$access_token" "$public_dns_name" "$instance_name")

	change_admin_password "$access_token" "$public_dns_name" "$admin_username" "$new_admin_password"
	create_new_user "$access_token" "$public_dns_name" "$user_username" "$user_password"

	header "Apache configuration"
	# Patch 001-events.conf
	# @param $1  dns name
	# @param $2  event id
	# @param $3  ssh user
	# @param $4  public dns name
	configure_apache "$public_dns_name" "$event_id" "$ssh_user" "$public_dns_name" "ssl"
}
