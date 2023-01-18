#!/bin/bash
mongo --quiet "mongodb://dbserver.internal.sapsailing.com:10201/winddb?replicaSet=archive" --eval 'db.TRACTRAC_CONFIGURATIONS.find({}, {TT_CONFIG_JSON_URL : 1}).toArray()' | grep -v ObjectId | jq -r '.[].TT_CONFIG_JSON_URL' | sort -u >/home/wiki/gitwiki/configuration/tractrac-json-urls
cd /home/wiki/gitwiki
git commit -m "Updated tractrac-json-urls" -a
git push
