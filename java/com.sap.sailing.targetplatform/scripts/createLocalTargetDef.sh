#!/bin/bash
source ./../../../configuration/correctFilePathInRelationToCurrentOs.sh
# generate a target definition pointing to the locally built p2 repo from the race-analysis-p2-remote target defintion
base="../definitions/race-analysis-p2"
remote_repo="https://p2.sapsailing.com/p2/sailing/"
#reading the filepath and editing it, so it fits for eclipse #currently save works for cygwin, gitbash and linux
local_repo=$(correct_file_path  "`readlink -f ../../com.sap.sailing.targetplatform.base/target/repository/`")
local_repo="file:/$local_repo"
# replace remote p2-repo URL with local repo URL
sed -e "/^<repository location=\"/ s@$remote_repo@$local_repo@" -e 's/target name="Race Analysis Target"/target name="Race Analysis Local Target"/' $base-remote.target > $base-local.target
