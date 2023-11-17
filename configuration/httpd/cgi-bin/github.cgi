#!/bin/bash
echo "
Hi Github,

we don't know if this was successful, but thanks for sending the webhook request.
"
BODY=$( cat )
echo "${BODY}" >/tmp/github-hook-body
REF=$( echo "${BODY}" | jq -r '.ref' )
PUSHER=$( echo "${BODY}" | jq -r '.pusher.email' )
logger -t github-cgi "ref ia $REF, pusher was $PUSHER"
# For testing:
#if [ "${PUSHER}" = "axel.uhl@sap.com" -a "${REF}" = "refs/heads/translation" ]; then
if [ "${PUSHER}" = "tmsatsls+github.tools.sap_service-tip-git@sap.com" -a "${REF}" = "refs/heads/translation" ]; then
  logger -t github-cgi "fetching translation branch from github.tools.sap and pushing it to ssh://trac@sapsailing.com/home/trac/git"
  cd /home/wiki/gitwiki
  sudo -u wiki git fetch sapsailing translation:translation >/tmp/git-fetch.out 2>/tmp/git-fetch.err
  sudo -u wiki git push origin translation:translation >/tmp/git-push.out 2>/tmp/git-push.err
fi
