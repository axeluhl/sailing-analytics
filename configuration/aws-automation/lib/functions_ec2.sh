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
# Get resources that should be automatically discovered
# @return   array of resource ids
# -----------------------------------------------------------
function get_auto_discover_resources(){
	aws_wrapper resourcegroupstaggingapi get-resources --tag-filters "Key=$autoDiscoverResourceTagName,Values=true,True,1,Yes,yes"
}

# -----------------------------------------------------------
# @param1   instance id
# @param2   tag key
# @return  tag value
# -----------------------------------------------------------
function get_tag_value_for_key(){
	aws_wrapper ec2 describe-instances --query "Reservations[*].Instances[*].Tags[?Key=='$2'].Value" --instance-ids $1 --output text
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
# Returns the instance id part of e.g. arn:aws:ec2:eu-west-2:017363970217:instance/i-096a32ca8c28bedbb
# param $1 resource arn
# @return  instance id
# -----------------------------------------------------------
function get_resource_id(){
	cut -d/ -f2 <<< $1
}

function is_resource_arn(){
	[[ $1 == arn:* ]]
}

function get_public_ip(){
	local_echo "Querying for the public ip of $1..."
	aws_wrapper ec2 describe-instances --query "Reservations[*].Instances[?PublicIpAddress=='$1'].InstanceId" --output text
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
# @param $1  ssh_user
# @param $2  public dns name
# -----------------------------------------------------------
function wait_for_ssh_connection(){
	local_echo -n "Connecting to $1@$2..."
	do_until_true ssh_prewrapper -q $1@$2 true 1>&2
}

function run_instance(){
	exit_on_fail aws_wrapper ec2 run-instances --image-id $(get_resource_id $image_id) --count 1 --instance-type $instance_type --key-name $key_name \
	--security-group-ids $(get_resource_id $instance_security_group_id) --user-data "$1" \
	--tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$instance_name}]"
}


# -----------------------------------------------------------
# Get user data of instance with specific instance id
# @param $1  instance id
# @return    user data of instance
# -----------------------------------------------------------
function get_user_data_from_instance() {
	aws_wrapper ec2 describe-instance-attribute --instance-id $1 --attribute userData --output text --query "UserData.Value" | sanitize 
}

# -----------------------------------------------------------
# Create instance and output response
# @param $1  user data
# -----------------------------------------------------------
function create_instance(){
	local_echo -e "Creating instance with following specifications:\n\nRegion: $region\nName: $instance_name\nShort name: $instance_short_name\nType: $instance_type\nBuild: $build_version\n\nUser data:\n${1}\n"
	json_instance=$(run_instance "$1")
	instance_id=$(echo "$json_instance" | get_attribute '.Instances[0].InstanceId')
	wait_instance_exists $instance_id
	aws_wrapper --region $region ec2 describe-instances --instance-ids $instance_id \
	--query "Reservations[*].Instances[0].{InstanceId:InstanceId, ImageId:ImageId, Type:InstanceType, PublicDNS:PublicDnsName, KeyName:KeyName, PrivateDnsName:PrivateDnsName, PrivateIpAddress:PrivateIpAddress}"\
	--output table

	echo $instance_id
}
