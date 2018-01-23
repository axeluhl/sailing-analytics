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
	require_build_version
	require_key_name
	require_key_file
	require_new_admin_password
	require_user_username
	require_user_password
}

# -----------------------------------------------------------
# Execute instance scenario
# @param $1  user data
# -----------------------------------------------------------
function instance_execute() {
	header "Instance Initialization"

	local user_data=$(build_configuration "MONGODB_HOST=$mongodb_host" "MONGODB_PORT=$mongodb_port" "MONGODB_NAME=$(alphanumeric $instance_name)" \
	"REPLICATION_CHANNEL=$(alphanumeric $instance_name)" "SERVER_NAME=$(alphanumeric $instance_name)" "USE_ENVIRONMENT=live-server" \
	"INSTALL_FROM_RELEASE=$build_version" "SERVER_STARTUP_NOTIFY=$default_server_startup_notify")

	instance_id=$(create_instance "$user_data")

	wait_for_ssh_connection $instance_id

	header "Event and user creation"

	port="8888"
	access_token=$(get_access_token $admin_username $admin_password $public_dns_name $port)
	event_id=$(create_event $access_token $public_dns_name $port $instance_name)
	response=$(change_admin_password $access_token $public_dns_name $port $admin_username $new_admin_password)
	user=$(create_new_user $access_token $public_dns_name $port $user_username $user_password)
}
