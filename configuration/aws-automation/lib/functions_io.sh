#!/usr/bin/env bash

CHOOSE_FROM_INSTANCES=":instance"
CHOOSE_FROM_SECURITY_GROUPS=":security-group"
CHOOSE_FROM_IMAGES=":image"
CHOOSE_FROM_CERTIFICATES=":certificate"
CHOOSE_FROM_LOAD_BALANCERS=":loadbalancer"

# -----------------------------------------------------------
# Prompt user for input and save value to variable
# @param $1  prompt message
# @param $2  default value
# @param $3  variable that is receiving the value
# @param $4  hide text
# @param $5  filter
# -----------------------------------------------------------
function require_input(){

  if [ ! -z "$5" ]; then
		declare -a option_keys=()
		declare -a option_values=()
		number_of_tagged_resources=0
		default_tagged_resource=""
		o=0
		for key in "${!RESOURCE_MAP[@]}"; do
			if [[ $key = *"$5"* ]]; then
				# if resource is tagged
				option_keys[o]=$key
				local tagged=$(echo ${RESOURCE_MAP[$key]} | cut -d, -f2)
				local name=$(echo ${RESOURCE_MAP[$key]} | cut -d, -f1)
				if [ $tagged == "true" ]; then
					option_values[o]="$(tput setaf 3)$name ($key)$(tput sgr0)"
					default_tagged_resource=$key
					((number_of_tagged_resources++))
				else
					option_values[o]="$name ($key)"
				fi
			fi
			((o++))
		done

		if [ $number_of_tagged_resources -eq 1 ] && [ "$force" == "true" ] || [ ${#option_keys[@]} -eq 1 ]; then
			read -r $3 <<< $default_tagged_resource
			return 0
		elif [ $number_of_tagged_resources -gt 0 ]; then
			selectedValue=""
			select option in ${option_values[@]}; do
				selectedValue=$option
				break
			done
			# workaround cant access option_keys from within select
			read -r $3 <<< $(echo $selectedValue  | sed  's/.*(\(.*\)).*/\1/')
			return 0
		else
			read_input_normal "$1" "$2" $3 $4
			return 0
		fi

	else
		read_input_normal "$1" "$2" $3 $4
	fi
}

function read_input_normal(){
	# if hide input
	if [ "$4" == "true" ]; then
		read -e -s -p "$1" -i "$2" $3
		# print new line after read -s
		echo
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
# @param $5  true if optional
# @param $6  true if text input be hidden
# @param $7  filter
# -----------------------------------------------------------
function require_variable(){
	# if parameter is empty
	if [ -z "$1" ]; then
		# if required variable value is empty
		if [ -z "${!2}" ]; then
			# if force is enabled
			if [ "$force" = true ] && [ -z "$7" ]; then
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
			# if force not enabled or filter is set
			# if variable value is optional, then value of "" is possible
			if [ "$5" == "true" ]; then
				require_input "$4" "$3" $2 $6 $7
			else
				# if variable value is not optional, repeat until value is set for variable
				while [[ -z "${!2}" ]]
				do
				  require_input "$4" "$3" $2 $6 $7
				done
			fi
		fi
	else
		# if parameter is not empty, read its value into required variable
		read -r "$2" <<< "$1"
		return 0
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

		RESOURCE_MAP[$arn]="${name:-"Unnamed"},$tagged"
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
		create_global_configuration
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

function create_global_configuration(){
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
