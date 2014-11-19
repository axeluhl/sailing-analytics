base="../definitions/race-analysis-p2"
remote_repo="http://p2.sapsailing.com/p2/sailing/"
local_repo="file://$(readlink -f ../../com.sap.sailing.targetplatform.base/gen/p2/)"

# replace remote p2-repo URL with local repo URL
sed -e "/^<repository location=\"/ s@$remote_repo@$local_repo@" $base-remote.target > $base-local.target
