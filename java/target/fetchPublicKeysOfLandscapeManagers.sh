#!/bin/bash
# Using the REST API end point $1/security/api/restsecurity/users_with_permission?permission=LANDSCAPE:MANAGE:$2
# this script obtains a JSON array containing user names which will then be passed as username[] multi-value parameter to
# the $1/landscape/api/landscape/get_ssh_keys_owned_by_user end point to obtain a JSON array with public SSH key objects.
# The "publicKey" attribute of each of these objects is the OpenSSH-formatted public key, ready for a .ssh/authorized_keys
# file. These OpenSSH-formatted keys will be sent to stdout, one key per line.
#
# If you need to authenticate your calls, you may either package a username/password combination into the URL for basic
# authentication, as in http://admin:admin@127.0.0.1:8888, or you may provide a bearer token as the third argument which
# will then be packaged into an 'Authorization: Bearer $3' header field.
if [ "$3" = "" ]; then
  BEARER_TOKEN=
else
  BEARER_TOKEN=( -H "Authorization: Bearer $3" )
fi
usernames=
for i in `curl -X GET "${BEARER_TOKEN[@]}" "$1/security/api/restsecurity/users_with_permission?permission=LANDSCAPE:MANAGE:$2" 2>/dev/null | jq -r '.[]'`; do
  usernames='username[]='$i'&'${usernames}
done
curl -X GET "${BEARER_TOKEN[@]}" "$1/landscape/api/landscape/get_ssh_keys_owned_by_user?${usernames}" 2>/dev/null| jq -r '.[].publicKey'