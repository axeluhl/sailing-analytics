#!/bin/bash
# Fetches the latest trac trac urls from the Mongo archive replica set, currently residing on the central mongo instance, 
# and commits the changes to the master branch of the main Git. Note that local state is prioritised if there is a conflict.
# Parameter 1 is an optional absolute path to the Git root. If unspecified, then the default is /home/wiki/gitwiki.
if [[ $# -eq 0 ]]; then
    GIT_ROOT=/home/wiki/gitwiki
else
    GIT_ROOT=$1
fi
PATH_TO_TRAC_TRAC_URLS="configuration/tractrac-json-urls"
urls=$(mongo --quiet "mongodb://dbserver.internal.sapsailing.com:10201/winddb?replicaSet=archive" --eval 'db.TRACTRAC_CONFIGURATIONS.find({}, {TT_CONFIG_JSON_URL : 1}).toArray()' | grep -v ObjectId | jq -r '.[].TT_CONFIG_JSON_URL' )
if [[ "$?" -ne 0 || "$urls" == "null" ]]; then
    echo "Mongo db returns null for tractrac url discovery" | notify-operators "MongoDB/tractrac urls issue"
    exit 1
else
    echo "${urls}" | sort -u >"${GIT_ROOT}/${PATH_TO_TRAC_TRAC_URLS}"
    cd "${GIT_ROOT}"
    git add "${GIT_ROOT}/${PATH_TO_TRAC_TRAC_URLS}"
    git commit -m "Updated tractrac-json-urls"
    git push
fi
