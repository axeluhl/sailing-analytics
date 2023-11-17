#!/bin/bash
echo "
"
BODY=$( cat )
echo "${BODY}" >/tmp/github-hook-body
REF=$( echo "${BODY}" | jq -r '.ref' )
PUSHER=$( echo "${BODY}" | jq -r '.pusher.email' )
logger -t github-cgi "ref ia $REF, pusher was $PUSHER"
# For testing:
#if [ "${PUSHER}" = "axel.uhl@sap.com" -a "${REF}" = "refs/heads/translation" ]; then
if [ "${PUSHER}" = "tmsatsls+github.tools.sap_service-tip-git@sap.com" -a "${REF}" = "refs/heads/translation" ]; then
  echo "Identified a push to refs/heads/translation by ${PUSHER}."
  echo "Fetching translation branch from github.tools.sap and pushing it to ssh://trac@sapsailing.com/home/trac/git"
  logger -t github-cgi "fetching translation branch from github.tools.sap and pushing it to ssh://trac@sapsailing.com/home/trac/git"
  cd /home/wiki/gitwiki
  sudo -u wiki git fetch sapsailing translation:translation 2>&1
  sudo -u wiki git push origin translation:translation 2>&1
else
  echo "Either pusher was not tmsatsls+github.tools.sap_service-tip-git@sap.com or ref was not refs/heads/translation. Not triggering."
fi
