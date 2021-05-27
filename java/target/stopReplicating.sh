#!/bin/bash
SERVER_PORT=$( cat env.sh | grep "^SERVER_PORT=" | tail -n 1 | sed -e 's/^SERVER_PORT=//' )
if [ "$1" = "-h" -o "$1" = "-?" ]; then
  echo "Usage: $0 [ {bearer-token} ]"
  echo "If no {bearer-token} is provided, username and password will be requested from the user."
else
  URL="http://127.0.0.1:${SERVER_PORT}/replication/replication?action=STOP_REPLICATING"
  if [ "$1" = "" ]; then
    read -p "Username: " USERNAME
    read -s -p "Password (not echoed): " PASSWORD
    curl -i -X POST -d "username=$USERNAME&password=$PASSWORD" "${URL}"
  else
    curl -i -X POST -H 'Authorization: Bearer '$1 "http://127.0.0.1:${SERVER_PORT}/replication/replication?action=STOP_REPLICATING"
  fi
fi
