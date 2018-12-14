#!/bin/bash
autostart_bundles=$(cat "$1" | grep "plugin id=.* startLevel=" | sed -e 's/^.* id="\(.*\)" autoStart="\(.*\)" startLevel="\([0-9]*\)".*$/\1 \3 \2/')
echo " *** Autostart Bundles ***"
echo "$autostart_bundles"
# These autostart bundles need to be updated accordingly in the .launch configuration
# so they have the correct startLevel and autostart settings. The autostart bundles
# may occur either in the target_bundles or the workspace_bundles attribute in the .launch
# configuration. All other entries in the .launch configuration are set to @default:default.

# The product consists of the features and plugins it lists. Its <configuration> section overrides
# the startLevel and autoStart defaults (4 / false) for a subset of the bundle list obtained from
# the features and plugins lists. The features that our product consists of are partitioned into
# ".runtime" and non-".runtime" features. The ".runtime" features obtain their bundles from the
# target platform, whereas the non-".runtime" features take their features from the workspace.

# In order to derive the target/workspace bundles for a launch configuration, the bun