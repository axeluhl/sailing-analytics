#!/bin/bash
target_bundles=$(cat "$1" | grep "plugin id=" | sed -e 's/^.* id="\([^"]*\)".*$/\1/')
echo " *** Target Bundles ***"
echo "$target_bundles"
