#!/bin/bash

echo "Making sure to stop Sailing Analytics server by its PID"
kill `cat server/server.pid`
sleep 5
rm server.pid

echo "Stopping mongodb..."
kill `cat mongodb-data/mongo.pid`
rm mongodb-data/mongo.pid
