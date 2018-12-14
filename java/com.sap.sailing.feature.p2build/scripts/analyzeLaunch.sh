#!/bin/bash
target_bundles=$(cat "$1" | grep 'key="target_bundles"' | sed -e 's/^.* value="\([^"]*\)"\/>/\1/' | sed -e 's/,/\n/g' | sed -e 's/^\(.*\)@\(.*\):\(.*\)$/\1 \2 \3/')
workspace_bundles=$(cat "$1" | grep 'key="workspace_bundles"' | sed -e 's/^.* value="\([^"]*\)"\/>/\1/' | sed -e 's/,/\n/g' | sed -e 's/^\(.*\)@\(.*\):\(.*\)$/\1 \2 \3/')
echo " *** Target Bundles ***"
echo "$target_bundles"
echo " *** Workspace Bundles ***"
echo "$workspace_bundles"

