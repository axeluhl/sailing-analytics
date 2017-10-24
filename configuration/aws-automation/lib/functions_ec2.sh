#!/usr/bin/env bash

# -----------------------------------------------------------
# Query for public dns name of instance
# @param $1  instance id
# @return    public dns name
# -----------------------------------------------------------
function query_public_dns_name(){
	local_echo "Querying for the instance public dns name..."
	local public_dns_name=$(query_public_dns_name_command $1)

	# not effective
	if is_error $?; then
		error "Querying for instance public dns name failed."
	else
		success "Public dns name of instance \"$instance_id\" is \"$public_dns_name\"."
		echo $public_dns_name | tr -d '\r'
	fi
}

function query_public_dns_name_command(){
	aws --region "$region" ec2 describe-instances --instance-ids "$1" --output text --query 'Reservations[*].Instances[*].PublicDnsName'
}

# -----------------------------------------------------------
# Creates $count instances inside $region with image $image_id and
# $instance_type and $user_data as well as according $tag_specifications,
# $security_group_ids and $key_name
# @return    instance id
# -----------------------------------------------------------
function run_instance(){
	local_echo "Creating instance..."
	write_user_data_to_file

	local command="aws --region $region ec2 run-instances"
	command+=$(add_param "region" $region)
	command+=$(add_param "image-id" $image_id)
	command+=$(add_param "count" $instance_count)
	command+=$(add_param "instance-type" $instance_type)
	command+=$(add_param "key-name" $key_name)
	command+=$(add_param "security-group-ids" $instance_security_group_ids)
	command+=$(add_param "user-data" 'file://${tmpDir}/$user_data_file')
	command+=$(add_param "tag-specifications" $(printf $tag_specifications $instance_name))

	local json_instance=$(eval "$command")
	local instance_id=$(get_instance_id "$json_instance")

	if is_valid_instance_id "$instance_id"; then
		success "Created instance \"$instance_id\""
		echo $json_instance
	else
		error "Failed creating instance."
	fi
}


# -----------------------------------------------------------
# Creates a file with user data for instance creation
# -----------------------------------------------------------
function write_user_data_to_file(){
	local CR_LF=$'\r'$'\n'

	local MONGODB_HOST="$mongodb_host"
	local MONGODB_PORT="$mongodb_port"
	local MONGODB_NAME="$(lower_trim $instance_name)"
	local REPLICATION_CHANNEL="$MONGODB_NAME"
	local SERVER_NAME="$MONGODB_NAME"
	local USE_ENVIRONMENT="live-server"
	local INSTALL_FROM_RELEASE="$(get_latest_release)"
	local SERVER_STARTUP_NOTIFY="$default_server_startup_notify"

	local content=
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

# NOT TESTED
# -----------------------------------------------------------
# Allocates an elastic ip address
# @return  elastic ip
# -----------------------------------------------------------
function allocate_address(){
	local_echo "Allocating elastic ip..."
	local json_result=$(allocate_address_command)

	if is_error $?; then
		error "Failed allocating elastic ip."
	else
		local elastic_ip=$(echo "$json_result" | jq -r '.PublicIp')
		success "Successfully allocated elastic ip: $elastic_ip."
		echo $elastic_ip
	fi
}

function allocate_address_command(){
	aws ec2 allocate_address
}

# NOT TESTED
# -----------------------------------------------------------
# Associate an elastic ip address with an instance
# @param $1  instance_id
# @param $2  elastic_ip
# -----------------------------------------------------------
function associate_address(){
	local_echo "Associating elastic ip \"$1\" with instance \"$2\"..."
	local json_result=$(associate_address_command $1 $2)

	if is_error $?; then
		error "Failed associating elastic ip."
	else
		success "Successfully associated elastic ip."
	fi
}

function associate_address_command(){
	aws ec2 associate-address --instance-id "$1" --public-ip "$2"
}

# -----------------------------------------------------------
# Creates a parameter from a key and value
# @param $1  key
# @param $2  value
# @return    result ("--key value")
# -----------------------------------------------------------
function get_availability_zones(){
	aws ec2 --region $region describe-availability-zones --query "AvailabilityZones[].ZoneName"
}

# -----------------------------------------------------------
# Wait until instance is recognized by AWS
# @param $1  instance_id
# -----------------------------------------------------------
function wait_instance_exists(){
	local_echo "Wait until instance \"$1\" is recognized by AWS..."
	local result=$(aws ec2 wait instance-exists --instance-ids $1)
}

# -----------------------------------------------------------
# Wait until ssh connection is established
# @param $1  key file to connect to instance
# @param $2  ssh user
# @param $3  dns name of instance
# -----------------------------------------------------------
function wait_for_ssh_connection(){
	echo -n "Connecting to $2@$3..."
	local status=
	while [[ $status != ok ]]
	do
		echo -n "."
		status=$(wait_for_ssh_connection_command $1 $2 $3)
		sleep $ssh_retry_interval
	done
	echo ""
	success "SSH Connection \"$2@$3\" is established."
}

function wait_for_ssh_connection_command(){
	ssh -o BatchMode=yes -o StrictHostKeyChecking=no -i $1 $2@$3 echo "ok" 2>&1 || true
}
