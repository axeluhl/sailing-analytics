#!/usr/bin/env bash

# -----------------------------------------------------------
# As long as variable is empty, prompt again.
# @param $1  prompt message
# @param $2  default value
# @param $3  variable that is receiving the value
# -----------------------------------------------------------
function ask(){
	while [[ -z "${!3}" ]]
	do
	  require_input "$1" "$2" $3
	done
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
# -----------------------------------------------------------
function require_variable(){
	# if parameter is empty
	if [ -z "$1" ]; then
		# if required variable is empty
		if [ -z "${!2}" ]; then
			# if force is enabled
			if [ "$force" = true ]; then
				# read default value into required variable
				read -r "$2" <<< "$3"
			fi
			# if force not enabled, ask user for input
			ask "$4" "$3" $2
		fi
	else
		# if parameter is not empty, read its value into required variable
		read -r "$2" <<< "$1"
	fi
}


# -----------------------------------------------------------
# Creates a parameter from a key and value
# @param $1  key
# @param $2  value
# @return    result ("--key value")
# -----------------------------------------------------------
function add_param() {
	if [ ! -z "$2" ]; then
		local result=" --$1 $2"
	fi
	echo "$result"
}

# -----------------------------------------------------------
# Constructs string of user data variable name and value plus linebreak
# @param $1  user data variable name
# @param $2  user data variable value
# @return    name=value (linebreak) if value not empty else nothing
# -----------------------------------------------------------
function add_user_data_variable(){
		if ! [ -z "$2" ]; then
			local CR_LF=$'\r'$'\n'
			local content="$1=$2"
			content+=$CR_LF
			echo "$content"
		fi
}
# -----------------------------------------------------------
# Checks if event id follows the right pattern
# @param $1  event id
# @return    true if event id is valid
# -----------------------------------------------------------
function is_valid_event_id(){
	[[ $1 =~ .{8}-.{4}-.{4}-.{4}-.{12} ]]
}

function is_valid_instance_id(){
	[[ $1 =~ i-.{17} ]]
}

# -----------------------------------------------------------
# Workaround: use stderr stream for console output
# @param $1  message
# -----------------------------------------------------------
function local_echo(){
	echo "$1" >&2
}

# -----------------------------------------------------------
# Check if return value is not equals 0
# @param $1  return value
# @return 0 if no error
# -----------------------------------------------------------
function is_error(){
	[ $1 -ne 0 ]
}

# -----------------------------------------------------------
# Checks if variable is a number
# @param $1  returnvariablevalue
# @return 0 if variable is a number
# -----------------------------------------------------------
function is_number(){
	[[ $1 =~ ^-?[0-9]+$ ]]
}

# -----------------------------------------------------------
# Check if variable is a number and its value is 200
# @param $1  variable
# @return 0 if value is 200
# -----------------------------------------------------------
function is_http_ok(){
	is_number $1 && [ $1 == 200 ]
}

function get_response(){
	echo "$1" | head -n-1
}

function get_status_code(){
	echo "$1" | tail -n1
}

# -----------------------------------------------------------
# Prompt user for input and save value to variable
# @param $1  prompt message
# @param $2  default value
# @param $3  variable that is receiving the value
# -----------------------------------------------------------
function require_input(){
	 read -e -p "$1" -i "$2" $3
}

region_ask_message="Please enter the region for the instance: "
instance_type_ask_message="Please enter the instance type: "
key_name_ask_message="Please enter the name of your keypair to connect to the instance: "
instance_name_ask_message="Please enter a name for the instance: (e.g \"WC Santander 2017\"): "
instance_short_name_ask_message="Please enter a short name for the instance (e.g. \"wcs17\"): "
key_file_ask_message="Please enter the file path of the keypair: "
new_admin_password_ask_message="Please enter a new password for the admin user: "
mongo_db_host_ask_message="Please enter the ip adress of the mongo db server: "
mongo_db_port_ask_message="Please enter the port of the mongo db server: "
user_username_ask_message="Please enter the username of your new user: "
user_password_ask_message="Please enter the password of your new user: "
public_dns_name_ask_message="Please enter the public dns name: "
ssh_user_ask_message="Please enter the ssh user: "

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
	require_variable "$ssh_user_param" ssh_user "" "$ssh_user_ask_message"
}
