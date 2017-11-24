#!/usr/bin/env bash

# Create elastic ip and associate it with instance
# ------------------------------------------------------

function associate_elastic_ip_start(){
	associate_elastic_ip_require
	associate_elastic_ip_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function associate_elastic_ip_require(){
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

function associate_elastic_ip_execute() {
	header "Elastic ip creation"

	local elastic_ip=$(allocate_address)
	associate_address "$instance_id" "$elastic_ip"

	header "Apache configuration"
	configure_apache "$elastic_ip" "$event_id" "$ssh_user" "$elastic_ip" "ssl"
}
