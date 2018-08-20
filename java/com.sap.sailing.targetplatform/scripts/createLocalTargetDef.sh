#!/bin/bash
# generate a target definition pointing to the locally built p2 repo from the race-analysis-p2-remote target defintion

base="../definitions/race-analysis-p2"
remote_repo="https://p2.sapsailing.com/p2/sailing/"
local_repo="file://`readlink -f ../../com.sap.sailing.targetplatform.base/target/repository/ | sed -e 's/\/cygdrive\/\(.\)/\/\1:/'`"

# replace remote p2-repo URL with local repo URL
sed -e "/^<repository location=\"/ s@$remote_repo@$local_repo@" -e 's/target name="Race Analysis Target"/target name="Race Analysis Local Target"/' $base-remote.target > $base-local.target
