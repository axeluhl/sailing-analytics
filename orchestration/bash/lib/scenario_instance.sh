#!/usr/bin/env bash

# Scenario for creating an ec2 instance
#
# Steps:
# Creates ec2 instance
# ------------------------------------------------------

function instance_start(){
	instance_require
	instance_preview
	instance_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function instance_require(){
	require_region
	require_load_balancer
	require_instance_type
	require_instance_security_group
	require_image
	require_instance_name
	require_instance_short_name
	require_mongodb_host
	require_mongodb_port
	require_ssh_user
	require_build_version
	require_key_name
	require_key_file
	require_event_name
	require_new_admin_password
}

function instance_preview(){
	header "Preview"
	image_id=$(get_resource_id $image)
	instance_security_group_id=$(get_resource_id $instance_security_group)
	user_data=$(build_user_data)
	local_echo "An instance will be created with the following specifications: "
	local_echo "To be implemented..."
	seek_confirmation "Do you want to continue?"
  if is_confirmed; then
		:
  else
		safeExit
	fi
}

# -----------------------------------------------------------
# Execute instance scenario
# @param $1  user data
# -----------------------------------------------------------
function instance_execute() {

	header "Instance Initialization"

	instance_id=$(create_instance "$user_data")
	public_dns_name=$(get_public_dns_name $instance_id)
	wait_for_ssh_connection $ssh_user $public_dns_name

	header "Event and user creation"

	local port="8888"
	event_id=$(configure_application "$public_dns_name" "$port" "$event_name" "$new_admin_password" "$user_username" "$user_password")

	header "Configuring ALB"

	listener_arn=$(get_first_https_listener $load_balancer)
	local target_group_arn=$(exit_on_fail create_target_group "S-dedicated-$instance_short_name")
	set_target_group_health_check "$target_group_arn" "HTTPS" "/index.html" "443" "5" "4" "2" "2"

	register_targets $target_group_arn $instance_id
	local domain=$(create_rule $listener_arn $instance_short_name $target_group_arn)

	header "Configuring Apache"

	append_macro_to_001_events_conf "$domain" "$event_id" "root" "$public_dns_name" "$port"

	header "Conclusion"

  success "Instance should be reachable through $public_dns_name:8888."
}

function build_user_data(){
	build_configuration "MONGODB_HOST=$mongodb_host" "MONGODB_PORT=$mongodb_port" "MONGODB_NAME=$(alphanumeric $instance_name)" \
	"REPLICATION_CHANNEL=$(alphanumeric $instance_name)" "SERVER_NAME=$(alphanumeric $instance_name)" "USE_ENVIRONMENT=live-server" \
	"INSTALL_FROM_RELEASE=$build_version" "SERVER_STARTUP_NOTIFY=$default_server_startup_notify"
}
