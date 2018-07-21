#!/bin/sh
ANDROID_RELEASE_BRANCH=android-xmake-release
RELEASE_BRANCH=rel-1.4
APP_MANIFESTS="mobile/com.sap.sailing.android.tracking.app/AndroidManifest.xml mobile/com.sap.sailing.buoy.positioning/AndroidManifest.xml mobile/com.sap.sailing.racecommittee.app/AndroidManifest.xml"
GIT_REMOTE=origin
# proxy can be requested with -x
PROXY_SETTINGS=
UPDATE_ANDROID_MANIFEST_VERSIONS=1
UPDATE_POM_VERSIONS=1
PERFORM_GIT_OPERATIONS=1

increment_version_code_and_set_version_name() {
  MANIFEST=$1
  NEW_VERSION_NAME=$2
  echo "Incrementing version code and version name for $MANIFEST"
  OLD_VERSION_CODE=`grep 'android:versionCode="[0-9]*"' $MANIFEST | sed -e 's/^.*android:versionCode="\([0-9]*\)".*$/\1/'`
  NEW_VERSION_CODE=$(($OLD_VERSION_CODE + 1))
  echo $MANIFEST: OLD_VERSION_CODE is $OLD_VERSION_CODE, NEW_VERSION_CODE is $NEW_VERSION_CODE
  echo $MANIFEST: Using versionName=\"$NEW_VERSION_NAME\"
  sed --in-place -e "s/android:versionCode=\"$OLD_VERSION_CODE\"/android:versionCode=\"$NEW_VERSION_CODE\"/" -e "s/android:versionName=\"\([^\"]*\)\"/android:versionName=\"$NEW_VERSION_NAME\"/" "$MANIFEST"
}

upgrade_pom_and_manifest_versions() {
  POM=$1
  NEW_POM_VERSION=$2
  POM_WITH_PARENT_SPEC_REMOVED="${POM}.parentspecremoved"
  PARENT_SPEC="${POM}.parentspec"
  RESTORED_POM_WITH_NEW_VERSION_AND_PARENT_SPEC="${POM}.restored"
  POM_BACKUP="${POM}.bak"
  cp "$POM" "$POM_BACKUP"
  # find the area to remove
  snip_area_start_line=`grep -n SNIP_MARKER_START pom.xml | sed -e 's/^\([0-9]*\):.*$/\1/'`
  snip_area_end_line=`grep -n SNIP_MARKER_END pom.xml | sed -e 's/^\([0-9]*\):.*$/\1/'`
  sed -e "$snip_area_start_line,$snip_area_end_line d" <"$POM" >"$POM_WITH_PARENT_SPEC_REMOVED"
  sed -e "1,$((snip_area_start_line - 1)) d" -e "$((snip_area_end_line + 1)),$ d" <"$POM" >"$PARENT_SPEC"
  cp "$POM_WITH_PARENT_SPEC_REMOVED" "$POM"
  echo mvn $PROXY_SETTINGS -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$NEW_POM_VERSION
  mvn $PROXY_SETTINGS -Dldi.releaseBuild=true -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$NEW_POM_VERSION
  sed --in-place -e 's/$/\\/' "$PARENT_SPEC"
  cat "$POM" | sed -e "$snip_area_start_line i \\`cat "$PARENT_SPEC"`" | sed -e "$snip_area_start_line,$snip_area_end_line s/\\\\$//" >"$RESTORED_POM_WITH_NEW_VERSION_AND_PARENT_SPEC"
  cp "$RESTORED_POM_WITH_NEW_VERSION_AND_PARENT_SPEC" "$POM"
}

if [ $# -eq 0 ]; then
    echo "$0 [-m -p -g -r <git-remote>] CR-Id: <TheJCWBCRId>"
    echo ""
    echo "Request a new Java Correction Workbench (JCWB) Correction Request ID at:"
    echo "  https://css.wdf.sap.corp/sap(bD1lbiZjPTAwMQ==)/bc/bsp/spn/jcwb/default.htm?newCMForProject=sapsailingcapture#"
    echo "See more usage details in the sapsailing.com wiki at:"
    echo "  https://wiki.sapsailing.com/wiki/info/landscape/building-and-deploying#building-deploying-stopping-and-starting-server-instances"
    echo "-m Disable upgrading the AndroidManifest.xml versionCode and versionName"
    echo "-p Disable upgrading the pom.xml and MANIFEST.MF versions"
    echo "-g Disable the final git push operation to refs/for/$RELEASE_BRANCH"
    echo "-r The git remote; defaults to origin"
    echo ""
    echo "Example: $0 CR-Id: 012003146900000486712016"
    exit 2
fi

options='mpgrx:'
while getopts $options option
do
    case $option in
        m) UPDATE_ANDROID_MANIFEST_VERSIONS=0;;
        p) UPDATE_POM_VERSIONS=0;;
        g) PERFORM_GIT_OPERATIONS=0;;
	r) GIT_REMOTE=$OPTARG;;
	x) PROXY_SETTINGS="-s configuration/maven-settings-proxy.xml -Dhttp.proxyHost=proxy.wdf.sap.corp -Dhttp.proxyPort=8080";;
        \?) echo "Invalid option"
            exit 4;;
    esac
done
shift $((OPTIND-1))

# change directory to the git root, assuming this script is in the configuration/ subfolder
GIT_DIR="`dirname \"$0\"`/.."
echo GIT_DIR: $GIT_DIR
JCWB_CHANGE_REQUEST_ID_GIT_TAG=$*
echo "Releasing based on Java Correction Workbench (JCWB) Correction Request ID tag $JCWB_CHANGE_REQUEST_ID_GIT_TAG"
cd "$GIT_DIR"

git fetch $GIT_REMOTE
git checkout $ANDROID_RELEASE_BRANCH
git merge -m "Merging $GIT_REMOTE/$ANDROID_RELEASE_BRANCH" $GIT_REMOTE/$ANDROID_RELEASE_BRANCH
git fetch $GIT_REMOTE $RELEASE_BRANCH:$RELEASE_BRANCH
git merge -m "Merging $RELEASE_BRANCH into $ANDROID_RELEASE_BRANCH, probably incorporating version setting to -SNAPSHOT" $GIT_REMOTE/$RELEASE_BRANCH
if [ "$PERFORM_GIT_OPERATIONS" = "1" ]; then
  git push $GIT_REMOTE $ANDROID_RELEASE_BRANCH:$ANDROID_RELEASE_BRANCH
fi

# Determine the version in the root pom.xml; if a -SNAPSHOT version, simply remove the -SNAPSHOT;
# otherwise, increment micro version by one and use the Tycho version plugin to update;
# For this to work, the <parent> specification needs to be removed from the top-level pom which later
# needs to be re-activated before committing everything
OLD_POM_VERSION=`head -n 15 pom.xml | grep '<version>.*</version>' | sed -e 's/^.*<version>\(.*\)<\/version>.*$/\1/'`
echo $OLD_POM_VERSION | grep -q -- "-SNAPSHOT"
if [ "$?" = "0" ]; then
  # found a -SNAPSHOT identifier; assuming this was for the next release already; simply remove -SNAPSHOT
  NEW_POM_VERSION=`echo $OLD_POM_VERSION | sed -e 's/-SNAPSHOT//'`
else
  # no -SNAPSHOT found; increment the micro version by one
  NEW_POM_VERSION=`echo $OLD_POM_VERSION | (IFS=. read major minor micro; echo ${major}.${minor}.$(($micro + 1)) )`
fi
echo OLD_POM_VERSION is $OLD_POM_VERSION, NEW_POM_VERSION is $NEW_POM_VERSION
if [ "$UPDATE_POM_VERSIONS" = "1" ]; then
  upgrade_pom_and_manifest_versions pom.xml $NEW_POM_VERSION
fi

# Patch the AndroidManifest.xml files to upgrade the android:versionCode sequential counter relevant for the PlayStore
# and the android:versionName which is what the user sees and which we expect to follow a major.minor version scheme,
# where this script increments the minor version by one
if [ "$UPDATE_ANDROID_MANIFEST_VERSIONS" = "1" ]; then
  for m in $APP_MANIFESTS; do
    increment_version_code_and_set_version_name $m $NEW_POM_VERSION
  done
fi

# Now commit the version changes and amend the commit using the change request ID tag:
git commit -a -m "Upgraded Android apps from version $OLD_POM_VERSION to $NEW_POM_VERSION"
git commit --amend -m "`git show -s --pretty=format:%s%n%n%b`
$JCWB_CHANGE_REQUEST_ID_GIT_TAG"
if [ "$PERFORM_GIT_OPERATIONS" = "1" ]; then
  git push $GIT_REMOTE $ANDROID_RELEASE_BRANCH:refs/for/$RELEASE_BRANCH
fi

echo "Now go to https://git.wdf.sap.corp/#/dashboard/self and vote on your change, using the \"Reply\" button."
echo "Remove the pMiosVoter because it has nothing good to say on Android. Then \"Submit\" your change to merge it to $RELEASE_BRANCH."
echo "After successful merge, launch a customer stage build here: https://xmake-ldi.wdf.sap.corp:8443/view/SAPSail/job/sapsailingcapture-Release/"
echo "When done, create a BCP update ticket. See https://wiki.wdf.sap.corp/wiki/display/NAAS/Mobile+Patch+Releases (remove the saprole parameter from the URL)"
echo "Copy the description of, e.g., https://support.wdf.sap.corp/sap/support/message/1670304244 to start with and adjust versions and commit IDs."
echo "Make sure you have your Metaman stuff updated, particularly the Release Notes section."
echo "Then wait for feedback on the release build being ready for smoke testing, do the smoke tests and report back in BCP. That's it :-)"
