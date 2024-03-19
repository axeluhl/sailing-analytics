#!/bin/bash

# Purpose: Create a mailing list in PATH_TO_STORE/landscapeManagersMailingList that contains the emails of all landscape managers, ie. those who
# have complete admin privileges in the aws environment.
BEARER_TOKEN="$1"
PATH_TO_STORE="$2"
NAME_TO_STORE_IN="landscapeManagersMailingList"
BASE_URL="https://security-service.sapsailing.com"
curl_output=$( curl -H 'X-SAPSSE-Forward-Request-To: master' -H 'Authorization: Bearer '${BEARER_TOKEN} "${BASE_URL}/security/api/restsecurity/users_with_permission?permission=LANDSCAPE:MANAGE:AWS" 2>/dev/null )
temp_file_path=$(mktemp /var/cache/emails_XXX)
echo $curl_output | jq -r .[] | while read user; do
    email=$(curl -H 'X-SAPSSE-Forward-Request-To: master' -H 'Authorization: Bearer '${BEARER_TOKEN} "${BASE_URL}/security/api/restsecurity/user?username=$user" 2>/dev/null| jq -r '.email' )
    echo $email >> $temp_file_path
done
if [[ ! -f "${PATH_TO_STORE}/${NAME_TO_STORE_IN}.bak" && -f "${PATH_TO_STORE}/${NAME_TO_STORE_IN}" ]]; then
    cp -f ${PATH_TO_STORE}/${NAME_TO_STORE_IN} ${PATH_TO_STORE}/${NAME_TO_STORE_IN}.bak
fi
if [[ "$(cat ${PATH_TO_STORE}/${NAME_TO_STORE_IN})" != "$(cat ${temp_file_path})" ]]; then
    mv "$temp_file_path" ${PATH_TO_STORE}/${NAME_TO_STORE_IN}
else
    rm ${temp_file_path}
fi