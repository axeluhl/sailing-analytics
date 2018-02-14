#!/usr/bin/env bash

# -----------------------------------------------------------
# Prompt user for input and save value to variable
# @param $1  prompt message
# @param $2  default value
# @param $3  variable that is receiving the value
# @param $4  hide text
# @param $5  array of resource names
# @param $6  array of resource arns
# -----------------------------------------------------------
function require_input(){
	declare -a _optionNames=()
	declare -a _optionValues=()
	if [ ! -z "$5" ] && [ ! -z "$6" ]; then
		_optionNames=("${!5}")
		_optionValues=("${!6}")
	fi

  if [ ${#_optionValues[@]} -gt 1 ]; then
			local_echo "$1"
			local_echo "--- Select by Name ---"
			o=0
			for name in ${optionNames[@]}; do
				local_echo "$((++o))) $name"
			done
			local_echo "--- Select by Resource ID ---"
			select option in ${_optionValues[@]};
			do
				read -r $3 <<< $option
				return 0
			done
  elif [ ${#_optionValues[@]} -eq 1 ]; then
			read -r $3 <<< ${_optionValues[0]}
	else
		if [ "$4" == "true" ]; then
			read -e -s -p "$1" -i "$2" $3
			# print new line after read -s
			echo
		else
			read -e -p "$1" -i "$2" $3
		fi
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
# @param $5  true if optional
# @param $6  true if text input be hidden
# @param $7  array of resource names
# @param $8  array of resource arns
# -----------------------------------------------------------
function require_variable(){
	declare -a optionNames=()
	declare -a optionValues=()
	if [ ! -z "$7" ] && [ ! -z "$8" ]; then
		optionNames=("${!7}")
		optionValues=("${!8}")
	fi

	# if parameter is empty
	if [ -z "$1" ]; then
		# if required variable value is empty
		if [ -z "${!2}" ]; then
			# if force is enabled
			if [ "$force" = true ]; then
				# if variable is optional
				if [ "$5" == "true" ]; then
					# variable should be empty
					read -r "$2" <<< ""
					return 0
				else
					# read default value into required variable
					read -r "$2" <<< "$3"
					return 0
				fi
			fi
			# if force not enabled
			# if variable value is optional, then value of "" is possible
			if [ "$5" == "true" ]; then
				require_input "$4" "$3" $2 $6 optionNames[@] optionValues[@]
			else
				# if variable value is not optional, repeat until value is set for variable
				while [[ -z "${!2}" ]]
				do
				  require_input "$4" "$3" $2 $6 optionNames[@] optionValues[@]
				done
			fi
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

function update_resources(){
	local resources_file=./lib/resources-$region.sh
	> $resources_file

	OLD_RESOURCE_JSON=$RESOURCE_JSON
	typeset -p OLD_RESOURCE_JSON >> $resources_file

	RESOURCES_ARRAY=($(get_resources))

	declare -A RESOURCE_MAP

	for resource in ${RESOURCES_ARRAY[@]}; do
		local arn=$(echo $resource | jq -c ".ResourceARN" -r | sanitize)
		local name=$(echo $resource | jq -c ".Tags[] | select(.Key==\"Name\") | .Value" -r | sanitize)
		local tagged=$(echo $resource | jq -c ".Tags[] | select(.Key==\"AutoDiscover\") | .Value" -r | sanitize)

		if [ ! -z "$tagged" ] && [ "$tagged" == "true" ] || [ "$tagged" == "True" ]; then
			tagged="true"
		else
			tagged="false"
		fi

		RESOURCE_MAP[$arn]="${name:-"Unnamed"}, $tagged"
	done

	typeset -p RESOURCE_MAP >> $resources_file
}

function init_resources(){
	local_echo "Checking if aws resources with tag key 'AutoDiscover' and value 'true' were added or removed in region $region..."
	RESOURCE_JSON=$(get_resources)

	if [ "$RESOURCE_JSON" != "$OLD_RESOURCE_JSON" ]; then
		  local_echo "Updating tagged aws resources..."
			update_resources
	else
		local_echo "Stored aws resources are up to date."
	fi

	if ! is_exists ~/.aws-automation/config-$region; then
		local_echo "Creating ~/.aws-automation/config-$region..."
		create_region_configuration
	fi

	if ! is_exists ~/.aws-automation/config; then
		local_echo "Creating ~/.aws-automation/config..."
		create_configuration
	fi
}

function create_region_configuration(){
	mkdir -p ~/.aws-automation
	touch  ~/.aws-automation/config-$region

	local config=""
	config+="default_instance_type=\n"
	config+="default_ssh_user=\n"
	config+="default_key_name=\n"
	config+="default_key_file=\n"
	config+="default_mongodb_host=\n"
	config+="default_mongodb_port=\n"

	echo -e $config >  ~/.aws-automation/config-$region
}

function create_configuration(){
	mkdir -p ~/.aws-automation
	touch  ~/.aws-automation/config

	local config=""
	config+="default_region=\n"
	config+="default_server_startup_notify=\n"
	config+="default_build_complete_notify=\n"

	echo -e $config >  ~/.aws-automation/config
}

function set_config_variable(){
	./lib/build-config.sh --bash ~/.aws-automation/config-$region "$1" "$2"
}

function get_config_variable(){
	./lib/build-config.sh --bash ~/.aws-automation/config-$region "$1"
}

function require_image_id(){
	require_variable "" image_id "" "$image_id_ask_message" "false" "false" TAGGED_IMAGE_NAMES[@] TAGGED_IMAGE_ARNS[@] ""
}

function require_event_name(){
	require_variable "$event_name_param" event_name "" "$event_name_ask_message" "true" "false"
}

function require_instance_security_group_id(){
	require_variable "" instance_security_group_id "" "$instance_security_group_id_ask_message" "false" "false" TAGGED_SECURITY_GROUP_NAMES[@] TAGGED_SECURITY_GROUP_ARNS[@]
}

function require_load_balancer(){
	require_variable "" load_balancer "" "$load_balancer_ask_message" "false" "false" TAGGED_LOADBALANCER_NAMES[@] TAGGED_LOADBALANCER_ARNS[@]
}

function require_instance(){
	require_variable "" instance "" "$instance_ask_message" "false" "false" TAGGED_INSTANCE_NAMES[@] TAGGED_INSTANCE_ARNS[@]
}

function require_region(){
	require_variable "$region_param" region "$default_region" "$region_ask_message" "false" "false"
}

function require_instance_type(){
	require_variable "$instance_type_param" instance_type "$default_instance_type" "$instance_type_ask_message" "false" "false"

}
function require_instance_name(){
	require_variable "$instance_name_param" instance_name "$default_instance_name" "$instance_name_ask_message" "false" "false"
}

function require_instance_short_name(){
	require_variable "$instance_short_name_param" instance_short_name "$default_instance_short_name" "$instance_short_name_ask_message" "false" "false"
}

function require_key_name(){
	if [ -z ${key_file+x} ] || [ ! -z "$key_file" ]; then
		require_variable "$key_name_param" key_name "$default_key_name" "$key_name_ask_message" "true" "false"
	fi
}

function require_key_file(){
	if [ -z ${key_name+x} ] || [ ! -z "$key_name" ]; then
		require_variable "$key_file_param" key_file "$default_key_file" "$key_file_ask_message" "true" "false"
	fi
}

function require_new_admin_password(){
	require_variable "$new_admin_password_param" new_admin_password "$default_new_admin_password" "$new_admin_password_ask_message" "true" "true"
}

function require_user_username(){
	if [ ! -z ${user_password+x} ] || [ ! -z "$user_password" ]; then
		require_variable "$user_username_param" user_username "$default_user_username" "$user_username_ask_message" "true" "false"
	fi
}

function require_user_password(){
	if [ ! -z ${user_name+x} ] || [ ! -z "$user_name" ]; then
	  require_variable "$user_password_param" user_password "$default_user_password" "$user_password_ask_message" "true" "true"
  fi
}

function require_public_dns_name(){
	require_variable "$public_dns_name_param" public_dns_name "" "$public_dns_name_ask_message" "false" "false"
}

function require_ssh_user(){
	require_variable "$ssh_user_param" ssh_user "$default_ssh_user" "$ssh_user_ask_message" "false" "false"
}

function require_super_instance(){
	require_variable "$super_instance_param" super_instance "$default_super_instance" "$super_instance_message"  "false" "false" TAGGED_INSTANCE_NAMES[@] TAGGED_INSTANCE_ARNS[@]
}

function require_description(){
	require_variable "$description_param" description "" "$description_message" "true" "false"
}

function require_contact_person(){
	require_variable "$contact_person_param" contact_person "$default_contact_person" "$contact_person_message" "true" "false"
}

function require_contact_email(){
	require_variable "$contact_email_param" contact_email "$default_contact_email" "$contact_email_message" "true" "false"
}

function require_build_version(){
	require_variable "$build_version_param" build_version "$latest_release" "$build_version_message" "false"
}

instance_ask_message="Please enter an instance to use: "
event_name_ask_message="Please enter an event name  (leave blank to skip event creation):"
instance_security_group_id_ask_message="Please select the security group for the instance: "
load_balancer_ask_message="Please select/enter the load balancer dns name: "
region_ask_message="Please enter the region you want to use (e.g. eu-west-2): "
instance_type_ask_message="Please enter the instance type (e.g. t2.medium): "
key_name_ask_message="Please enter the name of your keypair to connect to the instance (e.g. leon-keypair). Leave blank to use ~/.ssh key: "
instance_name_ask_message="Please enter a name for the instance: (e.g \"WC Santander 2017\"): "
instance_short_name_ask_message="Please enter a short name for the instance (e.g. \"wcs17\"): "
key_file_ask_message="Please enter the file path of the keypair or leave empty to use ~/.ssh key: "
new_admin_password_ask_message="Please enter a new password for the admin user (leave blank to skip password change): "
mongo_db_host_ask_message="Please enter the ip adress of the mongo db server: "
mongo_db_port_ask_message="Please enter the port of the mongo db server: "
user_username_ask_message="Please enter the username of your new user (leave blank to skip user creation): "
user_password_ask_message="Please enter the password of your new user (leave blank to skip user creation): "
ssh_user_ask_message="Please enter the ssh user to connect to the instance: "
super_instance_message="Please select/enter the dns name of the superior instance: "
description_message="Please enter a description for the server (leave blank to skip): "
contact_person_message="Please enter the name of a contact person (leave blank to skip): "
contact_email_message="Please enter the email of the contact person (leave blank to skip): "
build_version_message="Please enter a build version to use (releases.sapsailing.com): "
