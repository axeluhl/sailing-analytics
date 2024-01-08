#!/bin/bash

# Purpose: Create a mailing list in PATH_TO_STORE/landscapeManagersMailingList that contains the emails of all landscape managers, ie. those who
# have complete admin privileges in the aws environment.
BEARER_TOKEN="$1"
PATH_TO_STORE="$2"
NAME_TO_STORE_IN="landscapeManagersMailingList"
BASE_URL="https://security-service.sapsailing.com"
curl_output=$( curl -H 'X-SAPSSE-Forward-Request-To: master' -H 'Authorization: Bearer '${BEARER_TOKEN} "${BASE_URL}/security/api/restsecurity/users_with_permission?permission=LANDSCAPE:MANAGE:AWS" 2>/dev/null )
if [[ -f "${PATH_TO_STORE}/${NAME_TO_STORE_IN}" ]]; then
    mv -f ${PATH_TO_STORE}/${NAME_TO_STORE_IN} ${PATH_TO_STORE}/${NAME_TO_STORE_IN}.bak
fi
touch ${PATH_TO_STORE}/${NAME_TO_STORE_IN}
echo $curl_output | jq -r .[] | while read user; do
    email=$(curl -H 'X-SAPSSE-Forward-Request-To: master' -H 'Authorization: Bearer '${BEARER_TOKEN} "${BASE_URL}/security/api/restsecurity/user?username=$user" | jq -r '.email')
    echo $email >> ${PATH_TO_STORE}/${NAME_TO_STORE_IN}
done