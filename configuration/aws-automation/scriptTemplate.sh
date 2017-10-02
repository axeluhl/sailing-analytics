#!/usr/bin/env bash

# ##################################################
# AWS automation script for setting up SAP Sailing Analytics instance infrasctructure
#
version="1.0.0"               # Sets version variable

scriptPath="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

utilsLocation="${scriptPath}/lib/utils.sh"

# source utils.sh
if [ -f "${utilsLocation}" ]; then
  source "${utilsLocation}"
else
  echo "Please find the file util.sh and add a reference to it in this script. Exiting."
  exit 1
fi

# delete temp files when trapped
function trapCleanup() {
  echo ""
  deleteTemp
  die "Exit trapped. In function: '${FUNCNAME[*]}'"
}

function safeExit() {
  deleteTemp
  trap - INT TERM EXIT
  exit
}

function deleteTemp () {
   if is_dir "${tmpDir}"; then
     rm -r "${tmpDir}"
   fi
}

quiet=false
printLog=false
verbose=true
force=false
strict=false
debug=true
use_tmux=true
args=()

# Create temp directory with three random numbers and the process ID
tmpDir="./tmp/"
(umask 000 && mkdir "${tmpDir}") || {
  die "Could not create temporary directory! Exiting."
}

# Logging
# Log is only used when the '-l' flag is set.
# To never save a logfile change variable to '/dev/null'
# Save to standard user log location use: $HOME/Library/Logs/${scriptBasename}.log
# -----------------------------------
logFile="$HOME/Library/Logs/${scriptBasename}.log"


function mainScript() {

echo -n
if [ "$create_elb_standalone_instance_param" == "true" ]; then
	
	create_elb_standalone_instance
fi
}




usage() {
  echo -n "${scriptName} [OPTION]... [FILE]...

This is an AWS automation bash script for deploying SAP Sailing Analytics instances and their depending infrastructure

 ${bold}Options:${reset}
  -r, --region               AWS region (default: \"eu-west-2\" for London)
  -t, --instance-type        Instance type (default: \"t2.medium\")
  -k, --key-name             IAM keypair name 
  -f, --key-file             Path to keypair file
  -s, --security-group-ids   Security group ids of instance to create 
  -u, --user-username        Username of user to create
  -q, --user-password        Password of user to create
  -n, --instance-name        Name for instance (only letters and numbers)
  -l, --instance-short-name  Short name for instance
  -h, --hosted-zone-id	     Hosted zone id 
  -a, --admin-password	     New password for the admin user
  -m, --mongo-db-ip          Ip address of mongo db server
  -p, --mongo-db-port        Port of mongo db
  
      --create-elb-standalone-instance	 Create instance with elastic load balancer (default: \"false\")
      
	  --use-tmux             Use tmux for automatic log tailing of instance
	  --version              Output version information and exit
	  
	  
"
}

# Iterate over options breaking -ab into -a -b when needed and --foo=bar into
# --foo bar
optstring=h
unset options
while (($#)); do
  case $1 in
    # If option is of type -ab
    -[!-]?*)
      # Loop over each character starting with the second
      for ((i=1; i < ${#1}; i++)); do
        c=${1:i:1}

        # Add current char to options
        options+=("-$c")

        # If option takes a required argument, and it's not the last char make
        # the rest of the string its argument
        if [[ $optstring = *"$c:"* && ${1:i+1} ]]; then
          options+=("${1:i+1}")
          break
        fi
      done
      ;;

    # If option is of type --foo=bar
    --?*=*) options+=("${1%%=*}" "${1#*=}") ;;
    # add --endopts for --
    --) options+=(--endopts) ;;
    # Otherwise, nothing special
    *) options+=("$1") ;;
  esac
  shift
done
set -- "${options[@]}"
unset options

# Print help if no arguments were passed.
# Uncomment to force arguments when invoking the script
[[ $# -eq 0 ]] && set -- "--help"

# Read the options and set variables
while [[ $1 = -?* ]]; do
  case $1 in
    -h|--help) usage >&2; safeExit ;;
    --version) echo "$(basename $0) ${version}"; safeExit ;;
    -r|--region) shift; region_param=${1} ;;
	-t|--instance-type) shift; instance_type_param=${1} ;;
	-k|--key-name) shift; key_name_param=${1} ;;
	-f|--key-file) shift; key_file_param=${1} ;;
	-s|--security-group-ids) shift; security_group_ids_param=${1} ;;
	-u|--user-username) shift; user_username_param=${1} ;;
	-q|--user-password) shift; user_password_param=${1} ;;
	-n|--name) shift; instance_name_param=${1} ;;
	-l|--short-name) shift; instance_short_name_param=${1} ;;
	-h|--hosted-zone-id) shift; hosted_zone_id_param=${1} ;;
	-a|--admin-password) shift; new_admin_password_param=${1} ;;
	-m|--mongo-db-host) shift; mongo_db_host_param=${1} ;;
	-p|--mongo-db-port) shift; mongo_db_port_param=${1} ;;
	--create-elb-standalone-instance) shift; create_elb_standalone_instance_param=${1} ;;
	--use-tmux) shift; use_tmux=${1}	 ;;
    --endopts) shift; break ;;
    *) die "invalid option: '$1'." ;;
  esac
  shift
done

# Override default variables with parameter values 
region=${region_param:-$default_region}
instance_type=${instance_type_param:-$default_instance_type}
key_name=${key_name_param:-$default_key_name}
key_file=${key_file_param:-$default_key_file}
security_group_ids=${security_group_ids_param:-$default_security_group_ids}
user_data=${user_data_param:-$default_user_data}
instance_name=${instance_name_param:-$default_instance_name_param}
instance_short_name=${instance_short_name_param:-$default_instance_short_name}
hosted_zone_id=${hosted_zone_id_param:-$default_hosted_zone_id}
new_admin_password=${new_admin_password_param:-$default_new_admin_password}
user_username=${user_username_param:-$default_user_username}
user_password=${user_password_param:-$default_user_password}
MONGODB_HOST=${mongo_db_host_param:-$DEFAULT_MONGODB_HOST}
MONGODB_PORT=${mongo_db_port_param:-$DEFAULT_MONGODB_PORT}

# Store the remaining part as arguments.
args+=("$@")

############# RUN SCRIPT #############

# Trap bad exits with your cleanup function
trap trapCleanup EXIT INT TERM

# Set IFS to preferred implementation
IFS=$'\n\t'

# Exit on error. Append '||true' when you run the script if you expect an error.
set -o errexit

# Run in debug mode, if set
if ${debug}; then set -x ; fi

# Exit on empty variable
if ${strict}; then set -o nounset ; fi

# Bash will remember & return the highest exitcode in a chain of pipes.
# This way you can catch the error in case mysqldump fails in `mysqldump |gzip`, for example.
set -o pipefail

# Invoke the checkDependenices function to test for Bash packages.  Uncomment if needed.
# checkDependencies

# Run your script
mainScript

# Exit cleanlyd
safeExit