#!/bin/sh
cat list-of-dbs-to-merge | while read i; do
  if  grep $i list-of-live-dbs ; then
    echo "Found $i in live DBs"
    java -Dmongo.uri="mongodb://localhost:10203/security_service?replicaSet=live&retryWrites=true" -Ddefault.tenant.name=ARCHIVE-server -jar SecurityStoreMerger.jar "mongodb://localhost:10203/${i}?replicaSet=live&retryWrites=true" ${i}-server 2>&1 | tee storemerge_${i}.log
  elif grep $i list-of-archive-dbs ; then
    echo "Found $i in archive DBs"
    java -Dmongo.uri="mongodb://localhost:10203/security_service?replicaSet=live&retryWrites=true" -Ddefault.tenant.name=ARCHIVE-server -jar SecurityStoreMerger.jar "mongodb://localhost:10202/${i}" ${i}-server 2>&1 | tee storemerge_${i}.log
  else
    echo "DB $i not found in either archive or live DBs. Aborting"
    exit 1
  fi
done
