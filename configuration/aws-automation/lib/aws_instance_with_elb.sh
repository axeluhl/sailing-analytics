#!/usr/bin/env bash

function create_instance_with_elb(){
	user_input
	execute
}

function user_input(){
	input_region
	input_instance_type
	input_instance_name
	input_instance_short_name
	input_key_name
	input_key_file
	input_new_admin_password
	input_user_username
	input_user_password
}

function execute() {
	header "Instance Initialization"
	
	json_instance=$(run_instance)
	instance_id=$(get_instance_id "$json_instance")
	
	wait_instance_exists "$instance_id"
	public_dns_name=$(query_public_dns_name "$instance_id")
	
	header "SSH Connection"
	
	wait_for_ssh_connection "$key_file" "$ssh_user" "$public_dns_name"
	
	if $tail_instance; then
		tail_instance_logfiles "$ssh_user" "$public_dns_name"
	fi
	
	input_user_password
	
	header "Event and user creation"
	
	wait_for_access_token_resource "$admin_username" "$admin_password" "$public_dns_name"
	access_token=$(get_access_token "$admin_username" "$admin_password" "$public_dns_name")
	
	wait_for_create_event_resource "$public_dns_name"
	event_id=$(create_event "$access_token" "$public_dns_name")
	
	change_admin_password "$access_token" "$public_dns_name" "$admin_username" "$new_admin_password"
	create_new_user "$access_token" "$public_dns_name" "$user_username" "$user_password"
	
    # ignore new user for the moment because updating its priviliges via rest is not yet implemented
	
	header "Load balancer creation"
	
	load_balancer_name=$(echo "$instance_name" | trim)
	
	json_load_balancer=$(create_load_balancer_http "$load_balancer_name")
    # json_load_balancer=$(create_load_balancer_https "$load_balancer_name" "$certificate_arn")
	
    load_balancer_dns_name=$(get_elb_dns_name "$json_load_balancer")

	json_health_check=$(configure_health_check_http "$load_balancer_name")
    # configure_health_check_https "$load_balancer_name"	
	
	json_added_instance_response=$(add_instance_to_elb "$load_balancer_name" "$instance_id")
	added_instance_id=$(get_added_instance_from_elb "$json_added_instance_response" )
	
	header "Route53 record creation"
	
	route53_change_resource_record "$load_balancer_name" 60 "$load_balancer_dns_name"
	
	echo "Finished."
}

function run_instance(){
	local_echo "Creating instance..."
	create_user_data_file
	
	local command="aws --region $region ec2 run-instances"
	command+=$(add_param "region" $region)
	command+=$(add_param "image-id" $image_id)
	command+=$(add_param "count" $instance_count)
	command+=$(add_param "instance-type" $instance_type)
	command+=$(add_param "key-name" $key_name)
	command+=$(add_param "security-group-ids" $instance_security_group_ids)
	command+=$(add_param "user-data" 'file://${tmpDir}/$user_data_file')
	command+=$(add_param "tag-specifications" $(printf $tag_specifications $instance_name))
	
	json_instance=$(eval "$command") || true
	
	instance_id=$(get_instance_id "$json_instance")
	
	echo "$json_instance"
}

function create_user_data_file(){
	prepare_user_data_variables	
	write_user_data_to_file
}

function prepare_user_data_variables() {
	MONGODB_HOST="$mongodb_host"
	MONGODB_PORT="$mongodb_port"
	MONGODB_NAME="$(lower_trim $instance_name)"
	REPLICATION_CHANNEL="$MONGODB_NAME"
	SERVER_NAME="$MONGODB_NAME"
	USE_ENVIRONMENT="live-server"
	INSTALL_FROM_RELEASE="$(get_latest_release)"
	SERVER_STARTUP_NOTIFY="$default_server_startup_notify"
}

function write_user_data_to_file(){
	local CR_LF=$'\r'$'\n'

	content+="MONGODB_HOST=$MONGODB_HOST"
	content+=$CR_LF
	content+="MONGODB_PORT=$MONGODB_PORT"
	content+=$CR_LF
	content+="MONGODB_NAME=$MONGODB_NAME"
	content+=$CR_LF
	content+="INSTALL_FROM_RELEASE=$INSTALL_FROM_RELEASE"
	content+=$CR_LF
	content+="USE_ENVIRONMENT=$USE_ENVIRONMENT"
	content+=$CR_LF
	content+="REPLICATION_CHANNEL=$REPLICATION_CHANNEL"
	content+=$CR_LF
	content+="SERVER_NAME=$SERVER_NAME"
	content+=$CR_LF
	content+="SERVER_STARTUP_NOTIFY=$SERVER_STARTUP_NOTIFY"
	
	echo "$content" > "${tmpDir}/$user_data_file"
}





