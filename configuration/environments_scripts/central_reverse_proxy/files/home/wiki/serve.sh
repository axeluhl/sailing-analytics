#!/bin/bash
# kill any running gollums
command="ps axlw | grep rackup | grep ruby | awk '{ print \$3; }'"
kill -SIGTERM $(eval ${command})
sleep 10
if [[ -n "$(eval ${command})" ]]; then
  echo "Not terminated gracefully"
  kill -SIGKILL $(eval ${command})
fi
if [[ $# -eq 1 && "$1" == "stop" ]]; then
  echo "Stop command in parameter"
  exit 0
fi
# start gollum as a background process
# you can pipe output to /dev/null instead, if you don't want a log
cd /home/wiki
nohup rackup -p 4567 /home/wiki/config.ru 2>&1 >nohup.out &