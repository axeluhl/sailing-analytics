#!/usr/bin/env bash

# -----------------------------------------------------------
# Associates a chosen instance with a chosen application load balancer.
#
# Steps:
# Creates target group for instance.
# Configures health check of target group.
# Creates rule inside https listener of application load balancer.
# Registers target within target group.
# Configures apache of ec2 instance to revere proxy.
# -----------------------------------------------------------


function associate_alb_start(){
	associate_alb_require
	associate_alb_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function associate_alb_require(){
	require_instance
  require_instance_short_name
	require_load_balancer
	require_key_file
	require_ssh_user
}

function associate_alb_execute() {

	disable_aws_success_output
	instance_id=$(get_resource_id $instance)
	public_dns_name=$(get_public_dns_name $instance_id)
	enable_aws_success_output

	# create target group for instance
	local target_group_arn=$(exit_on_fail create_target_group "S-dedicated-$instance_short_name")
	exit_on_fail set_target_group_health_check "$target_group_arn" "HTTP" "/index.html" "80" "5" "4" "2" "2"

	listener_arn=$(exit_on_fail get_first_https_listener $load_balancer)
	exit_on_fail register_targets $target_group_arn $instance_id

	# create rule for host-based forwarding within alb
	local domain=$(exit_on_fail create_rule $listener_arn $instance_short_name $target_group_arn)

	header "Apache configuration"

	# configure apache with event macro
	exit_on_fail append_macro_to_001_events_conf $domain "" $ssh_user $public_dns_name "8888"
	exit_on_fail reload_httpd $ssh_user $public_dns_name
}
