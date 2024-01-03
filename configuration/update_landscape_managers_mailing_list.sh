#!/bin/bash

# Purpose: Create a mailing list in home_dir/mailinglists/landscapeManagersMailingList that contains the emails of all landscape managers, ie. those who
# have complete admin privileges in the aws environment.
BEARER_TOKEN="$1"
HOME_DIR="$2"
FOLDER_WITHIN_HOME="mailinglists"
NAME_TO_STORE_IN="landscapeManagersMailingList"
BASE_URL="https://security-service.sapsailing.com"
curl_output=$( curl -H 'X-SAPSSE-Forward-Request-To: master' -H 'Authorization: Bearer '${BEARER_TOKEN} "${BASE_URL}/security/api/restsecurity/users_with_permission?permission=LANDSCAPE:MANAGE:AWS" 2>/dev/null )
if [[ -f "${HOME_DIR}/${FOLDER_WITHIN_HOME}/${NAME_TO_STORE_IN}" ]]; then
    mv -f ${HOME_DIR}/${FOLDER_WITHIN_HOME}/${NAME_TO_STORE_IN} ${HOME_DIR}/${FOLDER_WITHIN_HOME}/${NAME_TO_STORE_IN}.bak
fi
touch ${HOME_DIR}/${FOLDER_WITHIN_HOME}/${NAME_TO_STORE_IN}
echo $curl_output | jq -r .[] | while read user; do
    email=$(curl -H 'X-SAPSSE-Forward-Request-To: master' -H 'Authorization: Bearer '${BEARER_TOKEN} "${BASE_URL}/security/api/restsecurity/user?username=$user" | jq -r '.email')
    echo $email >> ${HOME_DIR}/${FOLDER_WITHIN_HOME}/${NAME_TO_STORE_IN}
done