#!/usr/bin/env bash

# -----------------------------------------------------------
# Functions that are relevant for the treatment of user input and user configuration.
# -----------------------------------------------------------

# -----------------------------------------------------------
# Prompt user for input and save value to variable
# @param $1  prompt message
# @param $2  default value
# @param $3  variable that is receiving the value
# @param $4  hide text
# -----------------------------------------------------------
function require_input(){
  if [ ${#RESOURCE_MAP[@]} -eq 0 ]; then
		if [[ ( $NUMBER_OF_TAGGED_RESOURCES -eq 1 || ${#OPTION_KEYS[@]} -eq 1 ) && "$force" == "true" ]]; then
      local_echo "$1"
      if [ ! -z "$DEFAULT_TAGGED_RESOURCE" ]; then
        read -r $3 <<< $DEFAULT_TAGGED_RESOURCE
      else
        read -r $3 <<< ${OPTION_KEYS[0]}
      fi
		elif [ ${#OPTION_KEYS[@]} -gt 0 ]; then
      local_echo "$1"
			selectedValue=""
			select option in ${OPTION_VALUES[@]}; do
				selectedValue=$option
        break
			done
			# workaround cant access option_keys from within select
			read -r $3 <<< $(echo $selectedValue  | sed  's/.*(\(.*\)).*/\1/')

		else
			read_input_normal "$1" "$2" $3 $4
		fi

	else
		read_input_normal "$1" "$2" $3 $4
	fi

	unset OPTION_KEYS
	unset OPTION_VALUES
  unset RESOURCE_MAP
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
			if [ "$force" = true ] && [ ${#RESOURCE_MAP[@]} -eq 0 ]; then
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
				require_input "$4" "$3" $2 $6
			else
				# if variable value is not optional, repeat until value is set for variable
				while [[ -z "${!2}" ]]
				do
				  require_input "$4" "$3" $2 $6
				done
			fi
		fi
	else
		# if parameter is not empty, read its value into required variable
		read -r "$2" <<< "$1"
		return 0
	fi
}


function fill_resource_map_with_resources_of_type(){
	declare -a RESOURCE_ARRAY

	if [ "$1" == "ec2:launch-template" ]; then
		RESOURCE_ARRAY=($(get_array_with_launch_templates))
	else
		RESOURCE_ARRAY=($(get_array_with_resource_of_type $1))
	fi

	NUMBER_OF_TAGGED_RESOURCES=0
	DEFAULT_TAGGED_RESOURCE=""
	local o=0
	declare -A RESOURCE_MAP

	for resource in "${RESOURCE_ARRAY[@]}"; do
    local id=""

    if [ "$1" == "ec2:launch-template" ]; then
      id=$(echo "$resource" | jq -c ".LaunchTemplateId" -r | sanitize)
      tmp_name=$(echo "$resource" | jq -c ".LaunchTemplateName" -r | sanitize)
    else
      id=$(echo $resource | jq -c ".ResourceARN" -r | sanitize)
      tmp_name=$(echo $resource | jq -c ".Tags[] | select(.Key==\"Name\") | .Value" -r | sanitize)
    fi

		local name=${tmp_name:-"Unnamed"}
		local tagged=$(echo $resource | jq -c ".Tags[] | select(.Key==\"AutoDiscover\") | .Value" -r | sanitize)
		local display_name="$name ($id)"


		OPTION_KEYS[$o]=$id

		if [ ! -z "$tagged" ] && [ "$tagged" == "true" ] || [ "$tagged" == "True" ]; then
			DEFAULT_TAGGED_RESOURCE=$id
			option_value=$(highlight $display_name)
			((NUMBER_OF_TAGGED_RESOURCES++))
		else
			tagged="false"
			option_value=$display_name
		fi

		OPTION_VALUES[$o]=$option_value
		((o++))

		RESOURCE_MAP[$id]="$name,$tagged"
	done
}

function highlight(){
  local highlight_start=$(tput setaf 3)
  local reset=$(tput sgr0)
  echo $highlight_start$1$reset
}

# -----------------------------------------------------------
# Workaround: use stderr stream for console output
# @param $1  message
# -----------------------------------------------------------
function local_echo(){
	echo "$@" >&2
}

function init_resources(){
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
