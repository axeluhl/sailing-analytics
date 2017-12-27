#!/usr/bin/env bash

# -----------------------------------------------------------
# Query for public dns name of instance
# @param $1  instance id
# @return    public dns name
# -----------------------------------------------------------
function query_public_dns_name(){
	local_echo "Querying for the instance public dns name..."
	aws_wrapper ec2 describe-instances --instance-ids $1 --output text --query 'Reservations[*].Instances[*].PublicDnsName'
}

# -----------------------------------------------------------
# Query for default vpc id
# @return    default vpc id
# -----------------------------------------------------------
function get_default_vpc_id(){
	local_echo "Querying for the default vpc id..."
	aws_wrapper ec2 describe-vpcs --query 'Vpcs[?IsDefault==`true`].VpcId' --output text
}

# -----------------------------------------------------------
# Query for instance id by public dns name
# param $1 public dns name
# @return    instance id
# -----------------------------------------------------------
function get_instance_id(){
	local_echo "Querying for the instance id..."
	aws_wrapper aws ec2 describe-instances --query "Reservations[*].Instances[?PublicDnsName=='$1'].InstanceId" --output text
}

# -----------------------------------------------------------
# Allocates an elastic ip address
# @return  elastic ip
# -----------------------------------------------------------
function allocate_address(){
	local_echo "Allocating elastic ip..."
	aws_wrapper ec2 allocate-address | get_attribute '.PublicIp'
}

# -----------------------------------------------------------
# Associate an elastic ip address with an instance
# @param $1  instance_id
# @param $2  elastic_ip
# -----------------------------------------------------------
function associate_address(){
	local_echo "Associating instance \"$1\" with elastic ip \"$2\"..."
	aws_wrapper ec2 associate-address --instance-id $1 --public-ip $2
}

# -----------------------------------------------------------
# Get all availability zones of region
# @return  availability zones array
# -----------------------------------------------------------
function get_availability_zones(){
	aws_wrapper ec2 describe-availability-zones --query "AvailabilityZones[].ZoneName"
}

# -----------------------------------------------------------
# Wait until instance is recognized by AWS
# @param $1  instance_id
# -----------------------------------------------------------
function wait_instance_exists(){
	local_echo "Wait until instance \"$1\" is recognized by AWS..."
	aws_wrapper ec2 wait instance-exists --instance-ids $1
}

# -----------------------------------------------------------
# Wait until ssh connection is established
# @param $1  ssh user
# @param $2  dns name of instance
# -----------------------------------------------------------
function wait_for_ssh_connection(){
	echo -n "Connecting to $1@$2..."
	while ! ssh_wrapper -q $1@$2 true 1>&2; do
  	echo -n .
		sleep 2
	done
	success "SSH Connection \"$1@$2\" is established."
}


function run_instance(){
	local_echo "Creating instance..."

	local user_data=$(build_configuration "MONGODB_HOST=$mongodb_host" "MONGODB_PORT=$mongodb_port" "MONGODB_NAME=$(alphanumeric $instance_name)" \
	"REPLICATION_CHANNEL=$(alphanumeric $instance_name)" "SERVER_NAME=$(alphanumeric $instance_name)" "USE_ENVIRONMENT=live-server" \
	"INSTALL_FROM_RELEASE=$(get_latest_release)" "SERVER_STARTUP_NOTIFY=$default_server_startup_notify")

	aws_wrapper ec2 run-instances --image-id $image_id --count $instance_count --instance-type $instance_type --key-name $key_name \
	--security-group-ids $instance_security_group_ids --user-data "$user_data" \
	--tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$instance_name}]"
}
