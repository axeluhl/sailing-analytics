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
	require_image
	require_instance_name
	require_instance_short_name
	require_mongodb_host
	require_mongodb_port
	require_instance_security_group
	require_ssh_user
	require_build_version
	require_key_name
	require_key_file
	require_new_admin_password
}

# -----------------------------------------------------------
# Execute instance scenario
# @param $1  user data
# -----------------------------------------------------------
function master_instance_execute() {
	header "Master Instance Initialization"
	event_name=$instance_name
	image_id=$(get_resource_id $image)
	instance_security_group_id=$(get_resource_id $instance_security_group)

	local user_data_master=$(build_configuration "MONGODB_HOST=$mongodb_host" "MONGODB_PORT=$mongodb_port" "MONGODB_NAME=$(alphanumeric $instance_name)" \
	"REPLICATION_CHANNEL=$(alphanumeric $instance_name)" "SERVER_NAME=$(alphanumeric $instance_name)" "USE_ENVIRONMENT=live-master-server" \
	"INSTALL_FROM_RELEASE=$build_version" "SERVER_STARTUP_NOTIFY=$default_server_startup_notify")

	instance_id=$(exit_on_fail create_instance "$user_data_master")
	public_dns_name=$(get_public_dns_name $instance_id)

	wait_for_ssh_connection $ssh_user $public_dns_name

	header "Event and user creation"

	port="8888"
	event_id=$(configure_application $public_dns_name $port $event_name $new_admin_password $user_username $user_password)

	local user_data_replica=$(build_configuration "MONGODB_HOST=$mongodb_host" "MONGODB_PORT=$mongodb_port" "MONGODB_NAME=$(alphanumeric $instance_name)-replica" \
	"REPLICATION_CHANNEL=$(alphanumeric $instance_name)" "SERVER_NAME=$(alphanumeric $instance_name)" "USE_ENVIRONMENT=live-replica-server" \
	"REPLICATE_MASTER_SERVLET_HOST=$public_dns_name" "REPLICATE_MASTER_EXCHANGE_NAME=$(alphanumeric $instance_name)" "EVENT_ID=$event_id" "INSTALL_FROM_RELEASE=$build_version" "SERVER_STARTUP_NOTIFY=$default_server_startup_notify")

	local base64_user_data=$(echo "$user_data_replica" | base64 -w 0)
	local launch_template_for_replica=$(printf '{"UserData":"%s","ImageId":"%s","InstanceType":"%s","TagSpecifications":[{"ResourceType":"instance","Tags":[{"Key":"Name","Value":"%s"}]}], "SecurityGroupIds": ["%s"], "KeyName": "%s"}'\
	"${base64_user_data}" "$image_id" "$instance_type" "$instance_name (Replica)" "$instance_security_group_id" "$key_name")

	aws_wrapper ec2 create-launch-template --launch-template-name "Replica_$instance_short_name" --version-description "Replica for $instance_name." --launch-template-data $launch_template_for_replica

}
