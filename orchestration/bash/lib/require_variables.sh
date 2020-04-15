#!/usr/bin/env bash

# -----------------------------------------------------------
# Define variable properties and user messages here.
# -----------------------------------------------------------


# -----------------------------------------------------------
# Messages for user input.
# -----------------------------------------------------------

image_ask_message="Please select an image to use: "
instance_ask_message="Please enter an instance to use: "
event_name_ask_message="Please enter an event name  (leave blank to skip event creation): "
instance_security_group_ask_message="Please select the security group for the instance: "
load_balancer_ask_message="Please select or enter the load balancer dns name: "
region_ask_message="Please enter the region you want to use (e.g. eu-west-2): "
instance_type_ask_message="Please enter the instance type (e.g. t2.medium): "
key_name_ask_message="Please enter the name of your keypair to user for the instance (e.g. leon-keypair). "
instance_name_ask_message="Please enter a name for the instance: (e.g \"WC Santander 2017\"): "
instance_short_name_ask_message="Please enter a short name for the instance (e.g. \"wcs17\"): "
key_file_ask_message="Please enter the file path of the keypair or leave empty to use ~/.ssh key: "
new_admin_password_ask_message="Please enter a new password for the admin user (leave blank to skip password change): "
mongodb_host_ask_message="Please enter the ip adress/dns of the mongo db: "
mongodb_port_ask_message="Please enter the port of the mongo db: "
user_username_ask_message="Please enter the username of your new user (leave blank to skip user creation): "
user_password_ask_message="Please enter the password of your new user (leave blank to skip user creation): "
ssh_user_ask_message="Please enter the ssh user to connect to the instance: "
super_instance_message="Please select/enter the dns name of the superior instance: "
description_message="Please enter a description for the server (leave blank to skip): "
contact_person_message="Please enter the name of a contact person (leave blank to skip): "
contact_email_message="Please enter the email of the contact person (leave blank to skip): "
build_version_message="Please enter a build version to use (releases.sapsailing.com): "
launch_tempate_ask_message="Please select the launch template for the replica: "

IS_OPTIONAL="true"
IS_NOT_OPTIONAL="false"
SHOW_INPUT="false"
HIDE_INPUT="true"
NO_DEFAULT_VALUE=""
NOT_A_PARAMETER=""


# -----------------------------------------------------------
# Functions that are responsible for: Connection of variable to respective use rmessage. Setting if variable is required or can be left empty.
# Set the variables default value. Choose if user input will be shown or hidden (e.g. passwords). Choose whether the variable is a aws resources
# and can be selected. If yes then append a regex as last parameter (e.g. ValidImageARNRegex). Then the selection will be limited to resources
# of the associative array RESOURCE_MAP (functions_io.sh) that have a key matching the regex.
# -----------------------------------------------------------

function require_launch_template(){
	fill_resource_map_with_resources_of_type "ec2:launch-template"
	require_variable "$launch_template_param" launch_template "$NO_DEFAULT_VALUE" "$launch_tempate_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_image(){
	fill_resource_map_with_resources_of_type "ec2:image"
	require_variable "$image_param" image "$NO_DEFAULT_VALUE" "$image_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_event_name(){
	require_variable "$event_name_param" event_name "$NO_DEFAULT_VALUE" "$event_name_ask_message" "$IS_OPTIONAL" "$SHOW_INPUT"
}

function require_instance_security_group(){
	fill_resource_map_with_resources_of_type "ec2:security-group"
	require_variable "$security_group_param" instance_security_group "$NO_DEFAULT_VALUE" "$instance_security_group_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_load_balancer(){
	fill_resource_map_with_resources_of_type "elasticloadbalancing:loadbalancer"
	require_variable "$load_balancer_param" load_balancer "$NO_DEFAULT_VALUE" "$load_balancer_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT" $ValidLoadBalancerARNRegex
}

function require_instance(){
	fill_resource_map_with_resources_of_type "ec2:instance"
	require_variable "$instance_param" instance "$NO_DEFAULT_VALUE" "$instance_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_region(){
	require_variable "$region_param" region "$default_region" "$region_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_instance_type(){
	require_variable "$instance_type_param" instance_type "$default_instance_type" "$instance_type_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"

}
function require_instance_name(){
	require_variable "$instance_name_param" instance_name "$script_start_time" "$instance_name_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_instance_short_name(){
	require_variable "$instance_short_name_param" instance_short_name "$timestamp" "$instance_short_name_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_key_name(){
		require_variable "$key_name_param" key_name "$default_key_name" "$key_name_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_key_file(){
		require_variable "$key_file_param" key_file "$default_key_file" "$key_file_ask_message" "$IS_OPTIONAL" "$SHOW_INPUT"
}

function require_new_admin_password(){
	require_variable "$new_admin_password_param" new_admin_password "$default_new_admin_password" "$new_admin_password_ask_message" "$IS_OPTIONAL" "$HIDE_INPUT"
}

function require_user_username(){
	require_variable "$user_username_param" user_username "$default_user_username" "$user_username_ask_message" "$IS_OPTIONAL" "$SHOW_INPUT"

}

function require_mongodb_port(){
	  require_variable "$mongodb_port_param" mongodb_port "$default_mongodb_port" "$mongodb_port_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_mongodb_host(){
	  require_variable "$mongodb_host_param" mongodb_host "$default_mongodb_host" "$mongodb_host_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_user_password(){
	  require_variable "$user_password_param" user_password "$default_user_password" "$user_password_ask_message" "$IS_OPTIONAL" "$HIDE_INPUT"
}

function require_ssh_user(){
	require_variable "$ssh_user_param" ssh_user "root" "$ssh_user_ask_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_super_instance(){
	fill_resource_map_with_resources_of_type "ec2:instance"
	require_variable "$super_instance_param" super_instance "$NO_DEFAULT_VALUE" "$super_instance_message"  "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}

function require_description(){
	require_variable "$description_param" description "" "$description_message" "$IS_OPTIONAL" "$SHOW_INPUT"
}

function require_contact_person(){
	require_variable "$contact_person_param" contact_person "$default_contact_person" "$contact_person_message" "$IS_OPTIONAL" "$SHOW_INPUT"
}

function require_contact_email(){
	require_variable "$contact_email_param" contact_email "$default_contact_email" "$contact_email_message" "$IS_OPTIONAL" "$SHOW_INPUT"
}

function require_build_version(){
	require_variable "$build_version_param" build_version "$latest_release" "$build_version_message" "$IS_NOT_OPTIONAL" "$SHOW_INPUT"
}
