#!/usr/bin/env bash

# -----------------------------------------------------------
# Query for public dns name of instance
# @param $1  instance id
# @return    public dns name
# -----------------------------------------------------------
function query_public_dns_name(){
	local_echo "Querying for the instance public dns name..."
	aws_wrapper aws --region "$region" ec2 describe-instances --instance-ids "$1" --output text --query 'Reservations[*].Instances[*].PublicDnsName'
}

# -----------------------------------------------------------
# Allocates an elastic ip address
# @return  elastic ip
# -----------------------------------------------------------
function allocate_address(){
	local_echo "Allocating elastic ip..."
	aws_wrapper aws ec2 allocate-address | get_attribute '.PublicIp'
}

# -----------------------------------------------------------
# Associate an elastic ip address with an instance
# @param $1  instance_id
# @param $2  elastic_ip
# -----------------------------------------------------------
function associate_address(){
	local_echo "Associating instance \"$1\" with elastic ip \"$2\"..."
	aws_wrapper aws ec2 associate-address --instance-id "$1" --public-ip "$2"
}

function get_availability_zones(){
	aws_wrapper aws ec2 --region $region describe-availability-zones --query "AvailabilityZones[].ZoneName"
}

# -----------------------------------------------------------
# Wait until instance is recognized by AWS
# @param $1  instance_id
# -----------------------------------------------------------
function wait_instance_exists(){
	local_echo "Wait until instance \"$1\" is recognized by AWS..."
	aws_wrapper aws ec2 wait instance-exists --instance-ids $1
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
	if ! [ -z "$1" ]; then
		ssh -o BatchMode=yes -o StrictHostKeyChecking=no -i $1 $2@$3 echo "ok" 2>&1 || true
	else
		ssh -o BatchMode=yes -o StrictHostKeyChecking=no $2@$3 echo "ok" 2>&1 || true
	fi
}

# -----------------------------------------------------------
# Patch 001-events.conf
# @param $1  dns name
# @param $2  event id
# @param $3  keypair
# @param $4  ssh user
# @param $5  public dns name
# -----------------------------------------------------------
function configure_apache(){
	local content=$(ssh -o StrictHostKeyChecking=no -i "$3" "$4"@"$1" "cat /etc/httpd/conf.d/001-events.conf")
	local patched_content=$(comment_plain_ssl_entry "$content")
	patched_content=$(append_event_entry "$patched_content" "$1" "$2")
	echo "$patched_content" | ssh -o StrictHostKeyChecking=no -i $3 $4@$1 "cat > /etc/httpd/conf.d/001-events.conf"
	#local result=$(ssh -o StrictHostKeyChecking=no -i $3 $4@$5 "/etc/init.d/httpd reload")
}

function comment_plain_ssl_entry(){
	echo "$1" | sed -e '/Use Plain-SSL/ s/^#*/#/'
}

# -----------------------------------------------------------
# Append Use Event-SSL entry to string
# @param $1  content
# @param $2  dns name
# @param $2  event id
# -----------------------------------------------------------
function append_event_entry(){
	echo -e "$1\nUse Event $2 \"$3\" 127.0.0.1 8888"
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

	local command="aws_wrapper aws --region $region ec2 run-instances"
	command+=$(add_param "region" $region)
	command+=$(add_param "image-id" $image_id)
	command+=$(add_param "count" $instance_count)
	command+=$(add_param "instance-type" $instance_type)
	command+=$(add_param "key-name" $key_name)
	command+=$(add_param "security-group-ids" $instance_security_group_ids)
	command+=$(add_param "user-data" 'file://${tmpDir}/$user_data_file')
	command+=$(add_param "tag-specifications" $(printf $tag_specifications $instance_name))

	eval "$command"
}


# -----------------------------------------------------------
# Creates a file with user data for instance creation
# -----------------------------------------------------------
function write_user_data_to_file(){
	local MONGODB_HOST="$mongodb_host"
	local MONGODB_PORT="$mongodb_port"
	local MONGODB_NAME="$(lower_trim $instance_name)"
	local REPLICATION_CHANNEL="$MONGODB_NAME"
	local SERVER_NAME="$MONGODB_NAME"
	local USE_ENVIRONMENT="live-server"
	local INSTALL_FROM_RELEASE="$(get_latest_release)"
	local SERVER_STARTUP_NOTIFY="$default_server_startup_notify"

	local content=
	content+=$(add_user_data_variable "MONGODB_HOST" $MONGODB_HOST)
	content+=$(add_user_data_variable "MONGODB_PORT" $MONGODB_PORT)
	content+=$(add_user_data_variable "MONGODB_NAME" $MONGODB_NAME)
	content+=$(add_user_data_variable "INSTALL_FROM_RELEASE" $INSTALL_FROM_RELEASE)
	content+=$(add_user_data_variable "USE_ENVIRONMENT" $USE_ENVIRONMENT)
	content+=$(add_user_data_variable "REPLICATION_CHANNEL" $REPLICATION_CHANNEL)
	content+=$(add_user_data_variable "SERVER_NAME" $SERVER_NAME)
	content+=$(add_user_data_variable "SERVER_STARTUP_NOTIFY" $SERVER_STARTUP_NOTIFY)

	echo "$content" > "${tmpDir}/$user_data_file"
}
