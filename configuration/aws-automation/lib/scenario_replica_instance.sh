#!/usr/bin/env bash

#!/usr/bin/env bash

# Scenario for creating an instance
# ------------------------------------------------------

function replica_instance_start(){
	replica_instance_require
	replica_instance_execute
}

# -----------------------------------------------------------
# All these variables are needed for this scenario
# If one variable is not assigned or passed by parameter
# the user will be prompted to enter a value
# -----------------------------------------------------------
function replica_instance_require(){
	require_region
	require_launch_template
	require_ssh_user
	require_key_file
}

# -----------------------------------------------------------
# Execute instance scenario
# @param $1  user data
# -----------------------------------------------------------
function replica_instance_execute() {

	header "Replica Instance Initialization"

	instance_id=$(run_instance_from_launch_template $launch_template)
	wait_instance_exists $instance_id
	public_dns_name=$(get_public_dns_name $instance_id)
	user_data=$(get_user_data_from_instance $instance_id | base64 --decode)
	echo "$user_data"

	header "Conclusion"

	success "Instance should be reachable through $public_dns_name:8888."
}
