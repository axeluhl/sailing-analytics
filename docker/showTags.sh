#!/bin/bash
# Based on kizbitz/dockerhub-v2-api-organization.sh at https://gist.github.com/kizbitz/175be06d0fbbb39bc9bfa6c0cb0d4721

# Example for the Docker Hub V2 API
# Returns all images and tags associated with a Docker Hub organization account.
# Requires 'jq': https://stedolan.github.io/jq/

# set username, password, and organization

# TOKEN can be generated using docker login docker.sapsailing.com
TOKEN=$( cat ~/.docker/config.json | jq -r '.auths."docker.sapsailing.com".auth' )
if [ "${TOKEN}" = "null" ]; then
  # set username, password, and organization
  read -p "Username: " UNAME
  read -s -p "Password: " UPASS
  echo
  AUTH_HEADER="Authorization: Basic $( echo -n "${UNAME}:${UPASS}" | base64 )"
else
  AUTH_HEADER="Authorization: Basic ${TOKEN}"
fi
REPO=$1
if [ "$REPO" = "" ]; then
  echo "Usage: $0 account/repository [ <regexp1> \[ <regexp2> ... ] ]"
  echo
  echo "Shows all tags in account/repository, filtered by the regular expressions optionally provided"
  echo "Needs an authorization token in ~/.docker/.token."
  echo "Obtain one by invoking obtainToken.sh."
  exit 1
fi
shift

# -------
IMAGE_TAGS=$(curl -s -H "${AUTH_HEADER}" https://docker.sapsailing.com/v2/${REPO}/tags/list | jq -r '.tags|.[]')
for j in ${IMAGE_TAGS}
do
  if [ "$#" = "0" ]; then
    echo $j
  else
    found=0
    for k in $*
    do
      if [ "$found" = "0" ]; then
	# echo "...against $k"
	if [[ $j =~ $k ]]; then
	  found=1
	  echo "$j"
	fi
      fi
    done
  fi
done
