#!/bin/bash
autostart_bundles=$(cat "$1" | grep "plugin id=.* startLevel=" | sed -e 's/^.* id="\(.*\)" autoStart="\(.*\)" startLevel="\([0-9]*\)".*$/\1 \3 \2/')
echo " *** Autostart Bundles ***"
echo "$autostart_bundles"
