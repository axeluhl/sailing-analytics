#!/usr/bin/env bash

# AWS automation script for setting up SAP Sailing Analytics instance infrasctructure
# ------------------------------------------------------

version="1.0.0" # Sets version variable

scriptPath="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

utilsLocation="${scriptPath}/lib/utils.sh"

# source utils.sh which itself sources all other .sh files in the /lib directory
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
  exit 77
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
debug=false
args=()

# Create temp directory with three random numbers and the process ID
# in the name.  This directory is removed automatically at exit.
#tmpDir="/tmp/$RANDOM.$RANDOM.$RANDOM.$$/"
#(umask 000 && mkdir "${tmpDir}") || {
#  die "Could not create temporary directory! Exiting."
#}

# Logging (not used)
# To never save a logfile change variable to '/dev/null'
# Save to standard user log location use: $HOME/Library/Logs/${scriptBasename}.log
# -----------------------------------
logFile="$HOME/Library/Logs/${scriptBasename}.log"

function mainScript() {
echo -n

local config_file=~/.aws-automation/config

if is_exists ${config_file}; then
  source "${config_file}"
fi

require_region

local resource_file=./lib/resources-$region.sh
local region_config_file=~/.aws-automation/config-$region

if is_exists ${resource_file}; then
  source "${resource_file}"
fi

if is_exists ${region_config_file}; then
  source "${region_config_file}"
fi

init_resources

if $force; then
  error "--force option is currently not working."
  safeExit
fi

if $instance_scenario; then
	instance_start
	safeExit
fi

if $shared_instance_scenario; then
  shared_instance_start
	safeExit
fi

if $master_instance_scenario; then
  master_instance_start
	safeExit
fi

if $replica_instance_scenario; then
  replica_instance_start
	safeExit
fi

if $associate_alb_scenario; then
  associate_alb_start
	safeExit
fi

}

usage() {
  echo -n "${scriptName} [OPTION]...

 This is an AWS automation bash script for deploying SAP Sailing Analytics
 instances and their depending infrastructure

 ${bold}Parameter:${reset}
  -r, --region                  AWS region (e.g. \"eu-west-2\" for London)
  -d, --debug                   Debug mode

  // other parameters disabled for now

  ${bold}Scenarios:${reset}
  --instance                    Create instance
  --shared-instance             Create sub instance
  --master-instance             Create master instance
  --replica-instance            Create replica instance
  --associate-alb               Associate instance with existing application load balancer.

  ${bold}Other:${reset}
  --version                     Output version information and exit


  ${bold}Examples:${reset}
  Create instance:
  > ./aws-setup.sh --instance

  Create shared instance:
  > ./aws-setup.sh --shared-instance

  Associate instance with application load balancer:
  > ./aws-setup.sh --associate-alb

  Create master instance:
  > ./aws-setup.sh --master-instance

  Create replica instance:
  > ./aws-setup.sh --replica-instance
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

# Set default value of variable without parameter value to false
associate_alb_scenario=false
instance_scenario=false
shared_instance_scenario=false
master_instance_scenario=false
replica_instance_scenario=false

# Read the options and set variables
while [[ $1 = -?* ]]; do
  case $1 in
    -h|--help) usage >&2; safeExit ;;
    --version) echo "$(basename $0) ${version}"; safeExit ;;
    -r|--region) shift; region_param=${1} ;;
	-t|--instance-type) shift; instance_type_param=${1} ;;
	-k|--key-name) shift; key_name_param=${1} ;;
	-i|--key-file) shift; key_file_param=${1} ;;
	-s|--ssh-user) shift; ssh_user_param=${1} ;;
	-n|--instance-name) shift; instance_name_param=${1} ;;
	-l|--instance-short-name) shift; instance_short_name_param=${1} ;;
	-a|--new-admin-password) shift; new_admin_password_param=${1} ;;
	-p|--public-dns-name) shift; public_dns_name_param=${1} ;;
  -b|--super-instance) shift; super_instance_param=${1} ;;
  -m|--event-name) shift; event_name_param=${1} ;;
  -x|--mongodb-host) shift; mongodb_host_param=${1} ;;
  -y|--mongodb-port) shift; mongodb_name_param=${1} ;;
  -z|--build) shift; build_version_param=${1} ;;
  -w|--description) shift; description_param=${1} ;;
  -c|--contact-person) shift; contact_person_param=${1} ;;
  -e|--contact-email) shift; contact_email_param=${1} ;;
  -f|--force) force=true ;;
	-d|--debug) debug=true ;;
  --instance) instance_scenario=true ;;
  --shared-instance) shared_instance_scenario=true ;;
  --master-instance) master_instance_scenario=true ;;
  --replica-instance) replica_instance_scenario=true ;;
  --associate-alb) associate_alb_scenario=true ;;
    --endopts) shift; break ;;
    *) die "invalid option: '$1'." ;;
  esac
  shift
done

# Store the remaining part as arguments.
args+=("$@")

############# RUN SCRIPT #############

# Trap bad exits with your cleanup function
trap trapCleanup EXIT INT TERM

# Set IFS to preferred implementation
IFS=$'\n\t'

# Exit on error. Append '||true' when you run the script if you expect an error.
# set -o errexit

# Run in debug mode, if set
if ${debug}; then set -x ; fi

# Exit on empty variable
if ${strict}; then set -o nounset ; fi

# Bash will remember & return the highest exitcode in a chain of pipes.
# This way you can catch the error in case mysqldump fails in `mysqldump |gzip`, for example.
set -o pipefail

set -E
trap '[ "$?" -ne 77 ] || exit 77' ERR

# Invoke the checkDependenices function to test for Bash packages.  Uncomment if needed.
# checkDependencies

# Run script
mainScript

# Exit cleanlyd
safeExit
