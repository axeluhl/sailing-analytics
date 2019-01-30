#!/bin/sh
ANDROID_RELEASE_BRANCH=android-xmake-release
RELEASE_BRANCH=rel-1.4
APP_DIRS="mobile/com.sap.sailing.android.tracking.app/ mobile/com.sap.sailing.android.buoy.positioning.app/ mobile/com.sap.sailing.racecommittee.app/"
APP_GRADLE_PROPERTIES="gradle.properties"
FILES2SIGN=cfg/files2sign.json
VERSIONFILE=cfg/VERSION
GIT_REMOTE=origin

# proxy can be requested with -x
OPTION_PROXY_SETTINGS=
OPTION_UPDATE_ANDROID_VERSIONS=1
OPTION_PERFORM_GIT_OPERATIONS=1

# Read and update versionCode in build.gradle
# Based on new versionCode the versionNme will also be updated.
increment_version_code_and_set_version_name() {
  DIR=$1
  GRADLE_PROPERTIES_FILE=$DIR$APP_GRADLE_PROPERTIES
  echo "Incrementing version code and version name for $DIR"
  
  OLD_VERSION_CODE=`grep 'appVersionCode=[0-9]*' $GRADLE_PROPERTIES_FILE | sed -e 's/^.*appVersionCode=\([0-9]*\).*$/\1/'`
  NEW_VERSION_CODE=$(($OLD_VERSION_CODE + 1))
  NEW_VERSION_NAME=1.4.$NEW_VERSION_CODE

  echo $DIR: OLD_VERSION_CODE is $OLD_VERSION_CODE, NEW_VERSION_CODE is $NEW_VERSION_CODE
  echo $DIR: Using NEW_VERSION_NAME=\"$NEW_VERSION_NAME\"
  sed --in-place -e "s/appVersionCode=$OLD_VERSION_CODE/appVersionCode=$NEW_VERSION_CODE/" "$GRADLE_PROPERTIES_FILE"
  sed --in-place -e "s/appVersionName=\([^\"]*\)/appVersionName=$NEW_VERSION_NAME/" "$GRADLE_PROPERTIES_FILE"
}

# Update files2sign.json with new updated version name 
# replace, e.g. "version": "1.4.xy" with new one
update_files2sign() {
  echo "Update files2sign.json with new versionName $NEW_VERSION_NAME"
  OLD_VERSION_NAMES=`grep '"version": "1.4.[0-9]*"' $FILES2SIGN | sed -e 's/^.*\"version\": \"\(1.4.[0-9]*\)\".*$/\1/'`
  for OLD_VERSION_NAME in $OLD_VERSION_NAMES; do
    sed --in-place -e "s/\"version\": \"$OLD_VERSION_NAME\"/\"version\": \"$NEW_VERSION_NAME\"/" "$FILES2SIGN"
  done
}

options='mgrx:'
while getopts $options option
do
    case $option in
        m) OPTION_UPDATE_ANDROID_VERSIONS=0;;
        g) OPTION_PERFORM_GIT_OPERATIONS=0;;
        r) OPTION_GIT_REMOTE=$OPTARG;;
        x) PROXY_SETTINGS="-s configuration/maven-settings-proxy.xml -Dhttp.proxyHost=proxy.wdf.sap.corp -Dhttp.proxyPort=8080";;
        \?) echo "Invalid option"
            exit 4;;
    esac
done
shift $((OPTIND-1))

# change directory to the git root, assuming this script is in the configuration/ subfolder
GIT_DIR="`dirname \"$0\"`/.."
echo GIT_DIR: $GIT_DIR
cd "$GIT_DIR"

git fetch $GIT_REMOTE
git checkout $ANDROID_RELEASE_BRANCH
git merge -m "Merging $GIT_REMOTE/$ANDROID_RELEASE_BRANCH" $GIT_REMOTE/$ANDROID_RELEASE_BRANCH
git fetch $GIT_REMOTE $RELEASE_BRANCH:$RELEASE_BRANCH
git merge -m "Merging $RELEASE_BRANCH into $ANDROID_RELEASE_BRANCH, probably incorporating version setting to -SNAPSHOT" $GIT_REMOTE/$RELEASE_BRANCH
if [ "$OPTION_PERFORM_GIT_OPERATIONS" = "1" ]; then
  git push $GIT_REMOTE $ANDROID_RELEASE_BRANCH:$ANDROID_RELEASE_BRANCH
fi

# Patch the build.gradle files to upgrade the versionCode sequential counter relevant for the PlayStore
# and the versionName which is what the user sees and which we expect to follow a major.minor version scheme,
# where this script increments the minor version by one
if [ "$OPTION_UPDATE_ANDROID_VERSIONS" = "1" ]; then
  for m in $APP_DIRS; do
    increment_version_code_and_set_version_name $m
  done

  update_files2sign
fi

# Update the cfg/VERSION file with the new version
echo $NEW_VERSION_NAME >"$VERSIONFILE"

# Now commit the version changes and amend the commit using the change request ID tag:
git commit -a -m "Upgraded Android apps from version $OLD_VERSION_NAME to $NEW_VERSION_NAME"
git commit --amend -m "`git show -s --pretty=format:%s%n%n%b`"
if [ "$OPTION_PERFORM_GIT_OPERATIONS" = "1" ]; then
  git push $GIT_REMOTE $ANDROID_RELEASE_BRANCH:$RELEASE_BRANCH
fi

echo "Now go to https://git.wdf.sap.corp/#/dashboard/self and vote on your change, using the \"Reply\" button."
echo "Remove the pMiosVoter because it has nothing good to say on Android. Then \"Submit\" your change to merge it to $RELEASE_BRANCH."
echo "After successful merge, launch a customer stage build here: https://xmake-ldi.wdf.sap.corp:8443/view/SAPSail/job/sapsailingcapture-Release/"
echo "When done, create a BCP update ticket. See https://wiki.wdf.sap.corp/wiki/display/NAAS/Mobile+Patch+Releases \(remove the saprole parameter from the URL\)"
echo "Copy the description of, e.g., https://support.wdf.sap.corp/sap/support/message/1670304244 to start with and adjust versions and commit IDs."
echo "Make sure you have your Metaman stuff updated, particularly the Release Notes section."
echo "Then wait for feedback on the release build being ready for smoke testing, do the smoke tests and report back in BCP. That\'s it :-\)"
