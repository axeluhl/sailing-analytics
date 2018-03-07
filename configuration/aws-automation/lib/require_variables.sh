#!/usr/bin/env bash

instance_ask_message="Please enter an instance to use: "
event_name_ask_message="Please enter an event name  (leave blank to skip event creation):"
instance_security_group_id_ask_message="Please select the security group for the instance: "
load_balancer_ask_message="Please select/enter the load balancer dns name: "
region_ask_message="Please enter the region you want to use (e.g. eu-west-2): "
instance_type_ask_message="Please enter the instance type (e.g. t2.medium): "
key_name_ask_message="Please enter the name of your keypair to user for the instance (e.g. leon-keypair). "
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


function require_image_id(){
	require_variable "" image_id "" "$image_id_ask_message" "false" "false" $CHOOSE_FROM_IMAGES
}

function require_event_name(){
	require_variable "$event_name_param" event_name "" "$event_name_ask_message" "true" "false"
}

function require_instance_security_group_id(){
	require_variable "" instance_security_group_id "" "$instance_security_group_id_ask_message" "false" "false" $CHOOSE_FROM_SECURITY_GROUPS
}

function require_load_balancer(){
	require_variable "" load_balancer "" "$load_balancer_ask_message" "false" "false" $CHOOSE_FROM_LOAD_BALANCERS
}

function require_instance(){
	require_variable "" instance "" "$instance_ask_message" "false" "false" $CHOOSE_FROM_INSTANCES
}

function require_region(){
	require_variable "$region_param" region "$default_region" "$region_ask_message" "false" "false"
}

function require_instance_type(){
	require_variable "$instance_type_param" instance_type "$default_instance_type" "$instance_type_ask_message" "false" "false"

}
function require_instance_name(){
	require_variable "$instance_name_param" instance_name "$script_start_time" "$instance_name_ask_message" "false" "false"
}

function require_instance_short_name(){
	require_variable "$instance_short_name_param" instance_short_name "$timestamp" "$instance_short_name_ask_message" "false" "false"
}

function require_key_name(){
		require_variable "$key_name_param" key_name "$default_key_name" "$key_name_ask_message" "false" "false"
}

function require_key_file(){
		require_variable "$key_file_param" key_file "$default_key_file" "$key_file_ask_message" "true" "false"
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

function require_ssh_user(){
	require_variable "$ssh_user_param" ssh_user "$default_ssh_user" "$ssh_user_ask_message" "false" "false"
}

function require_super_instance(){
	require_variable "$super_instance_param" super_instance "$default_super_instance" "$super_instance_message"  "false" "false" $CHOOSE_FROM_INSTANCES
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
	require_variable "$build_version_param" build_version "$latest_release" "$build_version_message" "false" "false"
}
