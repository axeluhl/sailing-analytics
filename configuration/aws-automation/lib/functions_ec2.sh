#!/usr/bin/env bash

# -----------------------------------------------------------
# Query for public dns name of instance
# @param $1  instance id
# @return    public dns name
# -----------------------------------------------------------
function query_public_dns_name(){
	aws_wrapper ec2 describe-instances --instance-ids $1 --output text --query 'Reservations[*].Instances[*].PublicDnsName'
}

# -----------------------------------------------------------
# Query for default vpc id
# @return    default vpc id
# -----------------------------------------------------------
function get_default_vpc_id(){
	aws_wrapper ec2 describe-vpcs --query 'Vpcs[?IsDefault==`true`].VpcId' --output text
}

# -----------------------------------------------------------
# Query for instance id by public dns name
# param $1 public dns name
# @return    instance id
# -----------------------------------------------------------
function get_instance_id(){
	local_echo "Querying for the instance id of $1..."
	aws_wrapper ec2 describe-instances --query "Reservations[*].Instances[?PublicDnsName=='$1'].InstanceId" --output text
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
	aws_wrapper ec2 wait instance-exists --instance-ids $1 &> /dev/null
}

# -----------------------------------------------------------
# Wait until ssh connection is established
# @param $1  instance id
# -----------------------------------------------------------
function wait_for_ssh_connection(){
	public_dns_name=$(query_public_dns_name $1)
	local_echo -n "Connecting to $ssh_user@$public_dns_name..."
	do_until_true ssh_prewrapper -q $ssh_user@$public_dns_name true 1>&2
}

function run_instance(){
	local_echo -e "Creating instance with following specifications:\n\nRegion: $region\nName: $instance_name\nShort name: $instance_short_name\nType: $instance_type\nBuild: $build_version\n\nUser data:\n${1}\n"

	json_instance=$(aws_wrapper ec2 run-instances --image-id $image_id --count $instance_count --instance-type $instance_type --key-name $key_name \
	--security-group-ids $instance_security_group_ids --user-data "$1" \
	--tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$instance_name}]") || { safeExit; }

	echo "$json_instance" | get_attribute '.Instances[0].InstanceId'
}

# -----------------------------------------------------------
# Get user data of instance with specific instance id
# @param $1  instance id
# @return    user data of instance
# -----------------------------------------------------------
function get_user_data_from_instance() {
	aws_wrapper ec2 describe-instance-attribute --instance-id $1 --attribute userData --output text --query "UserData.Value" | sanitize | base64 --decode
}
