#!/bin/bash
# Uses mongodump / mongorestore to copy a database from the live replica set to
# the archive database. Afterwards, a comparison is made using the mongohash
# command
# Usage: archiveDb.sh <dbname>

DB=$1
DUMPBASEDIR=/var/lib/mongodb

echo -n "This will drop the database $DB in the archive DB before importing. Are you sure (y/n)? "
read CONFIRMATION
if [ "$CONFIRMATION" != "y" ]; then
  exit 2;
fi

cd "$DUMPBASEDIR"
mongodump --port 10203 --db $DB
# Drop the entire DB in the archive DB to ensure there are no left-over collections
echo "use $DB
db.dropDatabase()" | mongo --port 10202
mongorestore --noIndexRestore --drop --port 10202 dump
rm -rf dump/$DB
ORIGINAL_HASH=$(./mongohash -p 10203 -d $DB)
ARCHIVE_HASH=$(./mongohash -p 10202 -d $DB)
if [ $ORIGINAL_HASH = $ARCHIVE_HASH ]; then
  echo Hashes of old and new are equal: $ORIGINAL_HASH and $ARCHIVE_HASH. Dropping $DB and ${DB}-replica in live DB
  echo "use $DB
db.dropDatabase()
use ${DB}-replica
db.dropDatabase()" | mongo "mongodb://localhost:10203/$DB?replicaSet=live"
  echo "Don't forget to switch your server's env.sh so it has MONGODB_URI=\"mongodb://dbserver.internal.sapsailing.com:10202/$DB\""
  exit 0
else
  echo Hashes of old and new differ: $ORIGINAL_HASH and $ARCHIVE_HASH
  exit 1
fi
