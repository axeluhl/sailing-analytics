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
debug=false
args=()

# Create temp directory with three random numbers and the process ID
# in the name.  This directory is removed automatically at exit.
tmpDir="./tmp.$RANDOM.$RANDOM.$RANDOM.$$/"
(umask 000 && mkdir "${tmpDir}") || {
  die "Could not create temporary directory! Exiting."
}

# Logging (not used)
# To never save a logfile change variable to '/dev/null'
# Save to standard user log location use: $HOME/Library/Logs/${scriptBasename}.log
# -----------------------------------
logFile="$HOME/Library/Logs/${scriptBasename}.log"

function mainScript() {
echo -n

if $tail; then
  check_if_tmux_is_used
fi

if $instance; then
	instance_start
  if $associate_clb; then
  	associate_clb_start
  fi
  if $associate_elastic_ip; then
  	associate_elastic_ip_start
  fi
  if $associate_alb; then
  	associate_alb_start
  fi
  confirm_reset_panes
	safeExit
fi

if $tail ; then
	tail_start
  confirm_reset_panes
	safeExit
fi

}

usage() {
  echo -n "${scriptName} [OPTION]...

 This is an AWS automation bash script for deploying SAP Sailing Analytics
 instances and their depending infrastructure

 ${bold}Parameter:${reset}
  -r, --region                  AWS region (e.g. \"eu-west-2\" for London)
  -t, --instance-type           Instance type (e.g. \"t2.medium\")
  -k, --key-name                IAM keypair name (e.g. \"leonradeck-keypair\")
  -i, --key-file                Path to keypair file
  -s, --ssh-user                SSH user to connect to instance (e.g. \"root\")
  -u, --user-username           Username of user to create
  -q, --user-password           Password of user to create
  -n, --instance-name           Name for instance (e.g. \"WC Santander 2017\")
  -l, --instance-short-name     Short name for instance (e.g. subdomain \"wcs17\")
  -a, --new-admin-password      New password for the admin user
  -p, --public-dns-name         Dns name of instance (e.g. \"ec2-35-176...amazonaws.com\")
  -f, --force                   Skip user input and use default variables
  -d, --debug                   Debug mode

  ${bold}Scenarios:${reset}
  --instance                    Create instance
  --associate-alb               Add instance to existing application load balancer whos
                                listener is defined in variables_aws.sh. automatically
                                create necessary target group and host name rule.
                                Currently *.dummy.sapsailing.com is used as hardcoded
                                domain.
  --associate-clb               Create classic load balancer and add instance to it.
  --asslociate-elastic-ip       Create elastic ip and associate it with instance.
  --tail                        Tail logs from instance using tmux

  ${bold}Other:${reset}
  --version                  Output version information and exit


  ${bold}Examples:${reset}
  Create instance:
  > ./aws-setup.sh --instance

  Associate application load balancer:
  > ./aws-setup.sh --instance --associate-alb

  Associate instance with classic load balancer while
  automatically tailing important log files (tmux required):
  > ./aws-setup.sh --instance --associate-clb --tail

  Associate instance with elastic ip:
  > ./aws-setup.sh --instance --associate-elastic-ip

  Tail logfiles of running instance with dns name:
  > ./aws-setup.sh --tail --public-dns-name ec2-x.compute.amazonaws.com

  Create instance and use default values:
  > ./aws-setup.sh --instance --force

  Create instance and use default values except instance name
  and instance short name:
  > ./aws-setup.sh --instance --instance-name Test --instance-short-name t --force

  Associate instance with classic load balancer by
  passing all relevant parameters to script.
  Also use debug mode.
  > ./aws-setup.sh --region eu-west-2 --instance-type t2.medium
  --key-name leonradeck-keypair --key-file /cygdrive/c/Users/d069485/
  .ssh/leonradeck-keypair.pem --user-username test --user-password test
  --instance-name \"WC Santander 2017\" --instance-short-name test
  --new-admin-password admin -d --instance-with-load-balancer


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
associate_clb=false
associate_alb=false
associate_elastic_ip=false
instance=false
tail=false

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
	-u|--user-username) shift; user_username_param=${1} ;;
	-q|--user-password) shift; user_password_param=${1} ;;
	-n|--instance-name) shift; instance_name_param=${1} ;;
	-l|--instance-short-name) shift; instance_short_name_param=${1} ;;
	-a|--new-admin-password) shift; new_admin_password_param=${1} ;;
	-p|--public-dns-name) shift; public_dns_name_param=${1} ;;
  -f|--force) force=true ;;
	-d|--debug) debug=true ;;
	--associate-clb) associate_clb=true ;;
  --associate-alb) associate_alb=true ;;
  --associate-elastic-ip) associate_elastic_ip=true ;;
  --instance) instance=true ;;
	--tail) tail=true ;;
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

# Invoke the checkDependenices function to test for Bash packages.  Uncomment if needed.
# checkDependencies

# Run script
mainScript

# Exit cleanlyd
safeExit
