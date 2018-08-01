#!/usr/bin/env bash

# Logging and Colors
# ------------------------------------------------------
# Here we set the colors for our script feedback.
# Example usage: success "sometext"
#------------------------------------------------------

# Set Colors
bold=$(tput bold)
underline=$(tput sgr 0 1)
reset=$(tput sgr0)
purple='\033[0;35m'
red='\033[0;31m'
green='\033[0;32m'
tan=$(tput setaf 3)
blue='\033[0;36m'

function _alert() {
  if [ "${1}" = "emergency" ]; then
    local color="${bold}${red}"
  fi
  if [ "${1}" = "error" ]; then local color="${bold}${red}"; fi
  if [ "${1}" = "warning" ]; then local color="${red}"; fi
  if [ "${1}" = "success" ]; then local color="${green}"; fi
  if [ "${1}" = "debug" ]; then local color="${purple}"; fi
  if [ "${1}" = "header" ]; then local color="${bold}""${tan}"; fi
  if [ "${1}" = "input" ]; then local color="${bold}"; printLog="false"; fi
  if [ "${1}" = "info" ] || [ "${1}" = "notice" ]; then local color=""; fi

  # Don't use colors on pipes or non-recognized terminals
  # if [[ "${TERM}" != "xterm"* ]] || [ -t 1 ]; then color=""; reset=""; fi

  # Print to $logFile
  if [[ ${printLog} = "true" ]] || [ "${printLog}" == "1" ]; then
    echo -e "$(date +"%m-%d-%Y %r") $(printf "[%9s]" "${1}") ${_message}" >> "${logFile}";
  fi

  # Print to console when script is not 'quiet'
  if [[ "${quiet}" = "true" ]] || [ "${quiet}" == "1" ]; then
   return
  else
   echo -e "${color}$(printf "" "${1}") ${_message}${reset}" >&2;
  fi
}

function die ()       { local _message="${*} Exiting."; echo "$(_alert emergency)"; safeExit;}
function error ()     { local _message="\r${*}"; echo "$(_alert error)" >&2; }
function warning ()   { local _message="\r${*}"; echo "$(_alert warning)" >&2; }
function notice ()    { local _message="\r${*}"; echo "$(_alert notice)" >&2; }
function info ()      { local _message="\r${*}"; echo "$(_alert info)" >&2; }
function debug ()     { local _message="\r${*}"; echo "$(_alert debug)" >&2; }
function success ()   { local _message="\r${*}"; echo "$(_alert success)" >&2; }
function input()      { local _message="${*}"; echo "$(_alert input)" >&2; }
function header()     { local _message="\n========== ${*} ==========\n  "; echo "$(_alert header)"; }

# Log messages when verbose is set to "true"
verbose() {
  if [[ "${verbose}" = "true" ]] || [ "${verbose}" == "1" ]; then
    debug "$@"
  fi
}


# Source additional /lib files
# ------------------------------------------------------

# First we locate this script and populate the $SCRIPTPATH variable
# Doing so allows us to source additional files from this utils file.
SOURCE="${BASH_SOURCE[0]}"
while [ -h "${SOURCE}" ]; do # resolve ${SOURCE} until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "${SOURCE}" )" && pwd )"
  SOURCE="$(readlink "${SOURCE}")"
  [[ ${SOURCE} != /* ]] && SOURCE="${DIR}/${SOURCE}" # if ${SOURCE} was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
SOURCEPATH="$( cd -P "$( dirname "${SOURCE}" )" && pwd )"

wget -q --spider releases.sapsailing.com --timeout=5

if [ ! $? -eq 0 ]; then
  error "No internet connection. Exiting."
  safeExit
fi

if [ ! -d "${SOURCEPATH}" ]
then
  die "Failed to find library files expected in: ${SOURCEPATH}"
fi
for utility_file in "${SOURCEPATH}"/*.sh
do
  if [ -e "${utility_file}" ]; then

    # Don't source self
    if [[ "${utility_file}" == *"utils.sh"* ]]; then
      continue
    fi

    # Don't source configurator
    if [[ "${utility_file}" == *"build-config.sh"* ]]; then
      continue
    fi

    # Don't source resources file (will be sourced later)
    if [[ "${utility_file}" == *"resources"* ]]; then
      continue
    fi

    source "$utility_file"
  fi
done
