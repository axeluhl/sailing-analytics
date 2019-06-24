#!/bin/bash
# Based on kizbitz/dockerhub-v2-api-organization.sh at https://gist.github.com/kizbitz/175be06d0fbbb39bc9bfa6c0cb0d4721

# Example for the Docker Hub V2 API
# Returns all images and tags associated with a Docker Hub organization account.
# Requires 'jq': https://stedolan.github.io/jq/

# set username, password, and organization

# TOKEN_FILE can be generated using obtainToken.sh
TOKEN_FILE=~/.docker/.token
TOKEN=$(cat "${TOKEN_FILE}")
REPO=$1
if [ "$REPO" = "" ]; then
  echo "Usage: $0 account/repository [ <regexp1> \[ <regexp2> ... ] ]"
  echo
  echo "Delete all tags in account/repository that contain a substring"
  echo "matched by at least one of the regular expressions provided."
  echo "Needs an authorization token in ~/.docker/.token."
  echo "Obtain one by invoking obtainToken.sh."
  exit 1
fi
shift

# -------

echo "Deleting tags $* in repository ${REPO}..."

# delete images and/or tags
IMAGE_TAGS=$(curl -s -H "Authorization: JWT ${TOKEN}" https://hub.docker.com/v2/repositories/${REPO}/tags/?page_size=1000 | jq -r '.results|.[]|.name')
for j in ${IMAGE_TAGS}
do
  # echo "Checking tag $j"
  found=0
  for k in $*
  do
    if [ "$found" = "0" ]; then
      # echo "...against $k"
      if [[ $j =~ $k ]]; then
	found=1
	echo -n "  - ${j} ... "
	curl -X DELETE -s -H "Authorization: JWT ${TOKEN}" https://hub.docker.com/v2/repositories/${REPO}/tags/${j}/
	echo "DELETED"
      fi
    fi
  done
done
