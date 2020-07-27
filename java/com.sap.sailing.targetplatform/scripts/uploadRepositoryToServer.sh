#!/bin/bash
# Uploads repository to centralized location
# Make sure to have called ./createLocalBaseP2RepositoryLinux.sh before
# and make sure to have activated local-p2-admin target to check
# if everything went well

`dirname $0`/uploadSpecifiedRepositoryToServer.sh sailing com.sap.sailing.targetplatform.base
