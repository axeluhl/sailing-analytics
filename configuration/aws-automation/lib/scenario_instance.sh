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

function instance_execute() {
	header "Instance Initialization"

	# create instance
	instance_id=$(run_instance)

	# wait till instance is recognized by aws
	wait_instance_exists $instance_id

	# get public dns name of instance
	public_dns_name=$(query_public_dns_name $instance_id)

	description=$(aws ec2 describe-instances --instance-ids $instance_id \
	--query "Reservations[*].Instances[0].{InstanceId:InstanceId, ImageId:ImageId, Type:InstanceType, PublicDNS:PublicDnsName, KeyName:KeyName, PrivateDnsName:PrivateDnsName, PrivateIpAddress:PrivateIpAddress}"\
	--output table)

	if [ command_was_successful $0 ]; then
		success $description
	fi

	wait_for_ssh_connection $ssh_user $public_dns_name

	# if --tail option is passed then tail logfiles of instance within tmux
	if $tail; then
		tail_start
	fi

	header "Event and user creation"

	port="8888"
	access_token=$(get_access_token $admin_username $admin_password $public_dns_name $port)
	event_id=$(create_event $access_token $public_dns_name $port $instance_name)
	response=$(change_admin_password $access_token $public_dns_name $port $admin_username $new_admin_password)
	user=$(create_new_user $access_token $public_dns_name $port $user_username $user_password)
}
