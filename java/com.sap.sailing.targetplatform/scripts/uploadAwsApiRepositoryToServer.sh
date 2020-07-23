#!/bin/bash
# Uploads AWS SDK repository to centralized location
# Make sure to have called ./createLocalAwsApiP2Repository.sh before
# and make sure to have activated local-p2-admin target to check
# if everything went well
./uploadSpecifiedRepositoryToServer.sh aws-sdk com.amazon.aws.aws-java-api.updatesite