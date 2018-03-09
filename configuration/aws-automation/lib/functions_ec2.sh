#!/usr/bin/env bash

# -----------------------------------------------------------
# Functions that are relevant for the configuration of the ec2 instance.
# -----------------------------------------------------------

# -----------------------------------------------------------
# Get public dns name of instance
# @param $1  instance id
# @return    public dns name
# -----------------------------------------------------------
function get_public_dns_name(){
	local public_dns_name=$(aws_wrapper ec2 describe-instances --instance-ids $1 --output text --query 'Reservations[*].Instances[*].PublicDnsName')
	exit_on_fail validate_hostname $public_dns_name
}


# -----------------------------------------------------------
# Get resources all resources from aws
# @return  json array of resourceARNs
# -----------------------------------------------------------
function get_array_with_resource_of_type(){
	disable_aws_success_output
	# RESOURCE_ARRAY=($(aws_wrapper resourcegroupstaggingapi get-resources --resource-type-filters elasticloadbalancing:loadbalancer ec2:instance ec2:image ec2:security-group acm:certificate | jq -c ".ResourceTagMappingList[] | select(.ResourceARN)" -r | sanitize))
	aws_wrapper resourcegroupstaggingapi get-resources --resource-type-filters $1 | jq -c ".ResourceTagMappingList[] | select(.ResourceARN)" -r | sanitize
	enable_aws_success_output
}

# -----------------------------------------------------------
# Get launch templates as json
# param $1 public dns name
# @return  json array of launch templates
# -----------------------------------------------------------
function get_array_with_launch_templates(){
	disable_aws_success_output
	aws_wrapper ec2 describe-launch-templates | jq -c ".LaunchTemplates[]" -r | sanitize
	enable_aws_success_output
}


# -----------------------------------------------------------
# Get tag value of specified key of instance
# @param1   instance id
# @param2   tag key
# @return  tag value
# -----------------------------------------------------------
function get_tag_value_for_key(){
	aws_wrapper ec2 describe-instances --query "Reservations[*].Instances[*].Tags[?Key=='$2'].Value" --instance-ids $1 --output text
}

# -----------------------------------------------------------
# Get default vpc id
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
	local instance_id=$(aws_wrapper ec2 describe-instances --query "Reservations[*].Instances[?PublicDnsName=='$1'].InstanceId" --output text)
	exit_on_fail validate_instance_id $instance_id
}


# -----------------------------------------------------------
# Returns the instance id part of e.g. arn:aws:ec2:eu-west-2:017363970217:instance/i-096a32ca8c28bedbb
# param $1 resource arn
# @return  id part of arn
# -----------------------------------------------------------
function get_resource_id(){
	echo "${1##*/}"
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

# -----------------------------------------------------------
# Runs instance with specified user data. All other variables user inside function have to be initialized already.
# @param $1  user data
# @return  instance id
# -----------------------------------------------------------
function run_instance(){
	local instance_id=$(aws_wrapper ec2 run-instances --image-id $(get_resource_id $image_id) --count 1 --instance-type $instance_type --key-name $key_name \
	--security-group-ids $(get_resource_id $instance_security_group_id) --user-data "$1" \
	--tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$instance_name}]" | get_attribute '.Instances[0].InstanceId')
	exit_on_fail validate_instance_id $instance_id
}

# -----------------------------------------------------------
# Runs instance from specified launch template.
# @param $1  launch template id
# @return  instance id
# -----------------------------------------------------------
function run_instance_from_launch_template(){
	local instance_id=$(aws_wrapper ec2 run-instances --launch-template LaunchTemplateId="$1" | get_attribute '.Instances[0].InstanceId')
	exit_on_fail validate_instance_id $instance_id
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
	instance_id=$(run_instance "$1")
	wait_instance_exists $instance_id
	null=$(print_instance_description $instance_id)
	echo $instance_id
}

# -----------------------------------------------------------
# Prints instance description to screen.
# @param $1  instance id
# -----------------------------------------------------------
function print_instance_description(){
	aws_wrapper ec2 describe-instances --instance-ids $1 \
	--query "Reservations[*].Instances[0].{InstanceId:InstanceId, ImageId:ImageId, Type:InstanceType, PublicDNS:PublicDnsName, KeyName:KeyName, PrivateDnsName:PrivateDnsName, PrivateIpAddress:PrivateIpAddress}"\
	--output table
}
