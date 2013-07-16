#!/bin/bash
MONGOPORT=$@
MONGOIMPORT="/opt/mongodb/bin/mongoimport --upsert --port $MONGOPORT -d winddb"

echo "Uncompressing data..."
tar xvzf exported.tar.gz
echo "Importing data to $MONOGPORT..."
for i in *-$MONGOPORT.json; do
    c=`basename $i .json`;
    echo "$MONGOIMPORT -c $c $i"
    $MONGOIMPORT -c $c $i;
done
