#!/usr/bin/env bash

#!/usr/bin/env bash

# Scenario for creating an instance
# ------------------------------------------------------

function master_instance_start(){
	master_instance_require
	master_instance_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function master_instance_require(){
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
function master_instance_execute() {
	header "Master Instance Initialization"

	local user_data_replica=$(build_configuration "MONGODB_HOST=$mongodb_host" "MONGODB_PORT=$mongodb_port" "MONGODB_NAME=$(alphanumeric $instance_name)" \
	"REPLICATION_CHANNEL=$(alphanumeric $instance_name)" "SERVER_NAME=$(alphanumeric $instance_name)" "USE_ENVIRONMENT=live-replica-server" \
	"REPLICATE_MASTER_SERVLET_HOST=$public_dns_name" "REPLICATE_MASTER_EXCHANGE_HOST=$(alphanumeric $instance_name)" "EVENT_ID=123" "INSTALL_FROM_RELEASE=$build_version" "SERVER_STARTUP_NOTIFY=$default_server_startup_notify")

	local base64_user_data=$(echo "$user_data_replica" | base64 -w 0)
	local launch_template_for_replica=$(printf '{"UserData":"%s","ImageId":"%s","InstanceType":"%s","TagSpecifications":[{"ResourceType":"instance","Tags":[{"Key":"Name","Value":"%s"}]}], "SecurityGroupIds": ["%s"], "KeyName": "%s"}'\
	"${base64_user_data}" "$image_id" "$instance_type" "$instance_name (Replica)" "$instance_security_group_ids" "$key_name")

	aws_wrapper ec2 create-launch-template --launch-template-name "Replica_$instance_short_name" --version-description "Replica for $instance_name." --launch-template-data $launch_template_for_replica
	safeExit

	local user_data_master=$(build_configuration "MONGODB_HOST=$mongodb_host" "MONGODB_PORT=$mongodb_port" "MONGODB_NAME=$(alphanumeric $instance_name)" \
	"REPLICATION_CHANNEL=$(alphanumeric $instance_name)" "SERVER_NAME=$(alphanumeric $instance_name)" "USE_ENVIRONMENT=live-master-server" \
	"INSTALL_FROM_RELEASE=$build_version" "SERVER_STARTUP_NOTIFY=$default_server_startup_notify")

	instance_id=$(create_instance "$user_data_master")

	wait_for_ssh_connection $instance_id

	header "Event and user creation"

	port="8888"
	access_token=$(get_access_token $admin_username $admin_password $public_dns_name $port)
	event_id=$(create_event $access_token $public_dns_name $port $instance_name)
	response=$(change_admin_password $access_token $public_dns_name $port $admin_username $new_admin_password)
	user=$(create_new_user $access_token $public_dns_name $port $user_username $user_password)

}
