#!/bin/bash
GIT_ROOT=/home/wiki/gitwiki
mongo --quiet "mongodb://dbserver.internal.sapsailing.com:10201/winddb?replicaSet=archive" --eval 'db.TRACTRAC_CONFIGURATIONS.find({}, {TT_CONFIG_JSON_URL : 1}).toArray()' | grep -v ObjectId | jq -r '.[].TT_CONFIG_JSON_URL' | sort -u >"${GIT_ROOT}/configuration/tractrac-json-urls"
cd "${GIT_ROOT}"
git add "${GIT_ROOT}/configuration/tractrac-json-urls"
git commit -m "Updated tractrac-json-urls"
git push
