#!/usr/bin/env bash

# -----------------------------------------------------------
# Prompt user for input and save value to variable
# @param $1  prompt message
# @param $2  default value
# @param $3  variable that is receiving the value
# @param $4  array of resource names
# @param $5  array of resource arns
# -----------------------------------------------------------
function require_input(){
	declare -a _optionNames=()
	declare -a _optionValues=()
	if [ ! -z "$4" ] && [ ! -z "$5" ]; then
		_optionNames=("${!4}")
		_optionValues=("${!5}")
	fi

  if [ ${#_optionValues[@]} -gt 1 ]; then
			local_echo "--- Names ---"
			o=0
			for name in ${optionNames[@]}; do
				local_echo "$((++o))) $name"
			done
			local_echo "--- Resource IDs ---"
			select option in ${_optionValues[@]};
			do
				read -r $3 <<< $option
				break
			done
  elif [ ${#_optionValues[@]} -eq 1 ]; then
			read -r $3 <<< ${_optionValues[0]}
	else
		read -e -p "$1" -i "$2" $3
	fi
}


# -----------------------------------------------------------
# If the needed variable was not passed as a parameter and is not
# already set, then prompt the user to input a value and also
# show a default value. If the variable was passed by using a parameter,
# then set its value to the parameter value.
# @param $1  parameter value
# @param $2  variable
# @param $3  default value
# @param $4  prompt message
# @param $5  array of resource names
# @param $6  array of resource arns
# -----------------------------------------------------------
function require_variable(){
	declare -a optionNames=()
	declare -a optionValues=()
	if [ ! -z "$5" ] && [ ! -z "$6" ]; then
		optionNames=("${!5}")
		optionValues=("${!6}")
	fi

	# if parameter is empty
	if [ -z "$1" ]; then
		# if required variable is empty
		if [ -z "${!2}" ]; then
			# if force is enabled
			if [ "$force" = true ]; then
				# read default value into required variable
				read -r "$2" <<< "$3"
			fi
			# if force not enabled, ask user for input and repeat until value is set for variable
			while [[ -z "${!2}" ]]
			do
			  require_input "$4" "$3" $2 optionNames[@] optionValues[@]
			done
		fi
	else
		# if parameter is not empty, read its value into required variable
		read -r "$2" <<< "$1"
	fi
}

# -----------------------------------------------------------
# Workaround: use stderr stream for console output
# @param $1  message
# -----------------------------------------------------------
function local_echo(){
	echo "$@" >&2
}

function select_resource_names(){
	jq -c ".ResourceTagMappingList[] | select(.ResourceARN | contains(\"$1\")) | .Tags[] | select(.Key==\"Name\") | .Value" -r | sanitize
}

function select_resource_arns(){
	jq -c ".ResourceTagMappingList[] | select(.ResourceARN | contains(\"$1\")) | .ResourceARN" -r | sanitize
}

function get_names_for_resources(){
	declare -a resources=("${!1}")
	declare -a names=()

	for arn in ${resources[@]}; do
		name=$(echo "$RESOURCES" | select_resource_names $arn)
		if [ -z "$name" ]; then
			names+=("[Unnamed]")
		else
			names+=("$name")
		fi
	done
	echo ${names[@]}
}

function update_resources(){
	local resources_file=./lib/resources-$region.sh
	> $resources_file

	OLD_RESOURCES=$RESOURCES
	typeset -p OLD_RESOURCES >> $resources_file

	local filter="security-group"
	TAGGED_SECURITY_GROUP_ARNS=($(echo "$RESOURCES" | select_resource_arns $filter ))
	IFS=' ' read -ra TAGGED_SECURITY_GROUP_NAMES <<< "$(get_names_for_resources TAGGED_SECURITY_GROUP_ARNS[@])"
	typeset -p TAGGED_SECURITY_GROUP_NAMES TAGGED_SECURITY_GROUP_ARNS >> $resources_file

	local filter="image"
	TAGGED_IMAGE_ARNS=($(echo "$RESOURCES" | select_resource_arns $filter ))
	IFS=' ' read -ra TAGGED_IMAGE_NAMES <<< "$(get_names_for_resources TAGGED_IMAGE_ARNS[@])"
	typeset -p TAGGED_IMAGE_NAMES TAGGED_IMAGE_ARNS >> $resources_file

	local filter="instance"
	TAGGED_INSTANCE_ARNS=($(echo "$RESOURCES" | select_resource_arns $filter ))
	IFS=' ' read -ra TAGGED_INSTANCE_NAMES <<< "$(get_names_for_resources TAGGED_INSTANCE_ARNS[@])"
	typeset -p TAGGED_INSTANCE_NAMES TAGGED_INSTANCE_ARNS >> $resources_file

	local filter="certificate"
  TAGGED_CERTIFICATE_ARNS=($(echo "$RESOURCES" | select_resource_arns $filter ))
	IFS=' ' read -ra TAGGED_CERTIFICATE_NAMES <<< "$(get_names_for_resources TAGGED_CERTIFICATE_ARNS[@])"
	typeset -p TAGGED_CERTIFICATE_NAMES TAGGED_CERTIFICATE_ARNS >> $resources_file

	local filter="loadbalancer"
	TAGGED_LOADBALANCER_ARNS=($(echo "$RESOURCES" | select_resource_arns $filter ))
	IFS=' ' read -ra TAGGED_LOADBALANCER_NAMES <<< "$(get_names_for_resources TAGGED_LOADBALANCER_ARNS[@])"
	typeset -p TAGGED_LOADBALANCER_NAMES TAGGED_LOADBALANCER_ARNS >> $resources_file

}
function init_resources(){
	RESOURCES=$(get_auto_discover_resources)

	if [ "$RESOURCES" != "$OLD_RESOURCES" ]; then
			update_resources $resources_file
	fi

	update_configuration
}

function update_configuration(){
	mkdir -p ~/.aws-automation
	touch  ~/.aws-automation/config-$region

	init_config_variable default_key_name
	init_config_variable default_key_file
}

function init_config_variable(){
	local tmp=$(get_config_variable "$1")
	if [ -z $tmp ]; then
		set_config_variable "$1" ""
	else
		read -r "$1" <<< $tmp
	fi
}

function set_config_variable(){
	./lib/build-config.sh --bash ~/.aws-automation/config-$region "$1" "$2"
}

function get_config_variable(){
	./lib/build-config.sh --bash ~/.aws-automation/config-$region "$1"
}

function require_image_id(){
	require_variable "" image_id "" "$image_id_ask_message" TAGGED_IMAGE_NAMES[@] TAGGED_IMAGE_ARNS[@]
}

function require_instance_security_group_id(){
	require_variable "" instance_security_group_id "" "$instance_security_group_id_ask_message" TAGGED_SECURITY_GROUP_NAMES[@] TAGGED_SECURITY_GROUP_ARNS[@]
}
function require_load_balancer(){
	require_variable "" load_balancer "" "$load_balancer_ask_message" TAGGED_LOADBALANCER_NAMES[@] TAGGED_LOADBALANCER_ARNS[@]
}

function require_region(){
	require_variable "$region_param" region "$default_region" "$region_ask_message"
}

function require_instance_type(){
	require_variable "$instance_type_param" instance_type "$default_instance_type" "$instance_type_ask_message"

}
function require_instance_name(){
	require_variable "$instance_name_param" instance_name "$default_instance_name" "$instance_name_ask_message"
}

function require_instance_short_name(){
	require_variable "$instance_short_name_param" instance_short_name "$default_instance_short_name" "$instance_short_name_ask_message"
}

function require_key_name(){
	require_variable "$key_name_param" key_name "$default_key_name" "$key_name_ask_message"
}

function require_key_file(){
	require_variable "$key_file_param" key_file "$default_key_file" "$key_file_ask_message"
}

function require_new_admin_password(){
	require_variable "$new_admin_password_param" new_admin_password "$default_new_admin_password" "$new_admin_password_ask_message"
}

function require_user_username(){
	require_variable "$user_username_param" user_username "$default_user_username" "$user_username_ask_message"
}

function require_user_password(){
	require_variable "$user_password_param" user_password "$default_user_password" "$user_password_ask_message"
}

function require_public_dns_name(){
	require_variable "$public_dns_name_param" public_dns_name "" "$public_dns_name_ask_message"
}

function require_ssh_user(){
	require_variable "$ssh_user_param" ssh_user "$default_ssh_user" "$ssh_user_ask_message"
}

function require_super_instance(){
	require_variable "$super_instance_param" super_instance "$default_super_instance" "$super_instance_message" TAGGED_INSTANCE_NAMES[@] TAGGED_INSTANCE_ARNS[@]
	if is_resource_arn $super_instance; then
		local instance_id=$(get_resource_id $super_instance)
		super_instance=$(query_public_dns_name $instance_id)
	fi
}

function require_description(){
	require_variable "$description_param" description "$instance_name" "$description_message"
}

function require_contact_person(){
	require_variable "$contact_person_param" contact_person "$default_contact_person" "$contact_person_message"
}

function require_contact_email(){
	require_variable "$contact_email_param" contact_email "$default_contact_email" "$contact_email_message"
}

function require_build_version(){
	require_variable "$build_version_param" build_version "$latest_release" "$build_version_message"
}

instance_security_group_id_ask_message="Please select the security group for the instance: "
load_balancer_ask_message="Please select the load balancer: "
region_ask_message="Please enter the region for the instance: "
instance_type_ask_message="Please enter the instance type: "
key_name_ask_message="Please enter the name of your keypair to connect to the instance: "
instance_name_ask_message="Please enter a name for the instance: (e.g \"WC Santander 2017\"): "
instance_short_name_ask_message="Please enter a short name for the instance (e.g. \"wcs17\"): "
key_file_ask_message="Please enter the file path of the keypair or leave empty to use default ssh key: "
new_admin_password_ask_message="Please enter a new password for the admin user: "
mongo_db_host_ask_message="Please enter the ip adress of the mongo db server: "
mongo_db_port_ask_message="Please enter the port of the mongo db server: "
user_username_ask_message="Please enter the username of your new user: "
user_password_ask_message="Please enter the password of your new user: "
public_dns_name_ask_message="Please enter the public dns name: "
ssh_user_ask_message="Please enter the ssh user: "
super_instance_message="Please enter the dns name of the superior instance: "
description_message="Please enter a description for the server: "
contact_person_message="Please enter the name of a contact person: "
contact_email_message="Please enter the email of the contact person: "
build_version_message="Please enter a build version to use (releases.sapsailing.com): "
