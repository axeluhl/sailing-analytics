#!/bin/bash
MONGOPORTS=$@
MONGOIMPORT="/opt/mongodb/bin/mongoimport --upsert --port $PORT -d winddb"

echo "Uncompressing data..."
tar xvzf exported.tar.gz
echo "Importing data to $MONOGPORTS..."
for i in *.json; do c=`basename $i .json`; $MONGOIMPORT -c $c $i; done
