#!/usr/bin/env bash

# Create classic load balancer for instance
# ------------------------------------------------------

function associate_clb_start(){
	associate_clb_require
	associate_clb_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function associate_clb_require(){
	# will only be executed after instance creation
	:
}

function associate_clb_execute() {
	header "Associating classic load balancer"

	# create classic load balancer
  local load_balancer_dns_name=$(create_load_balancer_https $instance_name $certificate_arn | get_attribute '.DNSName')

	# configure health check of load balancer
  local json_health_check=$(configure_health_check_https $instance_name)

	# add instance to clb
	local json_added_instance_response=$(add_instance_to_clb $instance_name $instance_id)

	header "Apache configuration"

	# configure apache with event macro
	append_event_ssl_macro_to_001_events_conf $public_dns_name $event_id $ssh_user $public_dns_name '8888'
}
