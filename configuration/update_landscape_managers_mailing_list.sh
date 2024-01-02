#!/bin/bash

#Purpose: Create a mailing list in home_dir/mailinglists that contains the emails of all landscape managers.
BEARER_TOKEN="$1"
HOME_DIR="$2"
BASE_URL="https://security-service.sapsailing.com"
curl_output=$( curl -H 'X-SAPSSE-Forward-Request-To: master' -H 'Authorization: Bearer '${BEARER_TOKEN} "${BASE_URL}/security/api/restsecurity/users_with_permission?permission=LANDSCAPE:MANAGE:AWS" 2>/dev/null )
curl_output=$(echo $curl_output | sed "s/^\[//" | sed "s/\]$//" | sed 's|"||g' )
if [[ -f "${HOME_DIR}/mailinglists/landscapeManagersMailingList" ]]; then
    mv -f ${HOME_DIR}/mailinglists/landscapeManagersMailingList ${HOME_DIR}/mailinglists/landscapeManagersMailingList.bak
fi
oldIFS=$IFS
IFS=,
touch ${HOME_DIR}/mailinglists/landscapeManagersMailingList
for item in $curl_output; do
    email=$(curl -H 'X-SAPSSE-Forward-Request-To: master' -H 'Authorization: Bearer '${BEARER_TOKEN} "${BASE_URL}/security/api/restsecurity/user?username=$item" | jq '.email' | sed 's|"||g')
    echo $email >> ${HOME_DIR}/mailinglists/landscapeManagersMailingList
done
IFS=$oldIFS