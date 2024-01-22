#!/bin/bash
# kill any running gollums
kill -9 `ps axlw | grep rackup | grep ruby | awk '{ print $3; }'`
# start gollum as a background process
# you can pipe output to /dev/null instead, if you don't want a log
cd /home/wiki
nohup rackup -p 4567 /home/wiki/config.ru 2>&1 >nohup.out &