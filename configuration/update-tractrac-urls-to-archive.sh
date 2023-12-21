#!/bin/bash
GIT_ROOT=/home/wiki/gitwiki
PATH_TO_TRAC_TRAC_URLS="configuration/tractrac-json-urls"
urls=$(mongo --quiet "mongodb://dbserver.internal.sapsailing.com:10201/winddb?replicaSet=archive" --eval 'db.TRACTRAC_CONFIGURATIONS.find({}, {TT_CONFIG_JSON_URL : 1}).toArray()' | grep -v ObjectId | jq -r '.[].TT_CONFIG_JSON_URL' )
if [[ $urls == "null" ]]; then
    echo "Mongo db returns null for tractrac url discovery" | notify-operators "MongoDB/tractrac urls issue"
    exit 1
else 
    echo ${urls} | sort -u >"${GIT_ROOT}/${PATH_TO_TRAC_TRAC_URLS}"
fi
cd "${GIT_ROOT}"
git add "${GIT_ROOT}/${PATH_TO_TRAC_TRAC_URLS}"
git commit -m "Updated tractrac-json-urls"
git push
