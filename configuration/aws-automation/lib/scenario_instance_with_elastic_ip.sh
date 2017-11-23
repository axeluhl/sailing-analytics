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

	#instance_start

	instance_id='i-04b445f89f6f7d257'
	instance_name='Test'
	public_dns_name='ec2-35-177-107-67.eu-west-2.compute.amazonaws.com'
	event_id="123"
	header "Elastic ip creation"

	local elastic_ip=$(allocate_address)
	associate_address "$instance_id" "$elastic_ip"

	header "Apache configuration"

	configure_apache "$elastic_ip" "$event_id" "$ssh_user" "$elastic_ip" ""

	echo "Finished."
}
