#!/bin/bash
# Download release tar ball and release-notes.txt and upload to releases.sapsailing.com
# Usage:
#   ./github-download-release-assets.sh {BEARER_TOKEN} {release-name-prefix}
# For example:
#  ./github-download-release-assets.sh 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 main-
# which will download the latest build of the main branch (main-xxxxxxxxxxx) and upload
# it to releases.sapsailing.com.
# Note the "-" at the end of the "main-" prefix specifier; this way we're making name
# clashes with releases whose name happens to start with "main" unlikely. This
# also suggests you shouldn't name releases "main-abcde-xxxxxxxxxxxx" because they would
# produce false matches for the "main-" prefix.
# For the "main" branch/release a special step is added for backward compatibility with
# java/target/refreshInstance.sh: the release is linked to from an additional folder
# called "build-xxxxxxxxxx" (using the same timestamp in its name), with the tar ball
# and the release notes being symbolic links to the corresponding files in the main-xxxxxxxxx
# folder.
BEARER_TOKEN="${1}"
RELEASE_NAME_PREFIX="${2}"
RELEASES=$( curl -L -H 'Authorization: Bearer '${BEARER_TOKEN} https://api.github.com/repos/SAP/sailing-analytics/releases 2>/dev/null )
RELEASE_NOTES_TXT_ASSET_ID=$( echo "${RELEASES}" | jq -r 'sort_by(.created_at) | reverse | map(select(.name | startswith("'${RELEASE_NAME_PREFIX}'")))[0].assets[] | select(.content_type=="text/plain").id' )
RELEASE_TAR_GZ_ASSET_ID=$( echo "${RELEASES}" | jq -r 'sort_by(.created_at) | reverse | map(select(.name | startswith("'${RELEASE_NAME_PREFIX}'")))[0].assets[] | select(.content_type=="application/x-tar").id' )
RELEASE_FULL_NAME=$( echo "${RELEASES}" | jq -r 'sort_by(.created_at) | reverse | map(select(.name | startswith("'${RELEASE_NAME_PREFIX}'")))[0].assets[] | select(.content_type=="application/x-tar").name' | sed -e 's/\.tar\.gz$//')
# For backward compatibility with deployed versions of java/target/refreshInstance.sh
# which may be looking for a default release named "build-...", in case the release
# is a "main-..." release, additionally create a corresponding folder and symbolic links named "build-..."
RELEASE_NAME=$( echo ${RELEASE_FULL_NAME} | sed -e 's/^\(.*\)-\([0-9]*\)$/\1/' )
RELEASE_TIMESTAMP=$( echo ${RELEASE_FULL_NAME} | sed -e 's/^\(.*\)-\([0-9]*\)$/\2/' )
echo "Found release ${RELEASE_FULL_NAME} with name ${RELEASE_NAME} and time stamp ${RELEASE_TIMESTAMP}, notes ID is ${RELEASE_NOTES_TXT_ASSET_ID}, tarball ID is ${RELEASE_TAR_GZ_ASSET_ID}"
curl -o ${RELEASE_FULL_NAME}.tar.gz -L -H 'Accept: application/octet-stream' -H 'Authorization: Bearer '${BEARER_TOKEN} 'https://api.github.com/repos/SAP/sailing-analytics/releases/assets/'${RELEASE_TAR_GZ_ASSET_ID}
curl -o release-notes.txt -L -H 'Accept: application/octet-stream' -H 'Authorization: Bearer '${BEARER_TOKEN} 'https://api.github.com/repos/SAP/sailing-analytics/releases/assets/'${RELEASE_NOTES_TXT_ASSET_ID}
FOLDER=releases/${RELEASE_FULL_NAME}
ssh trac@sapsailing.com mkdir -p ${FOLDER}
scp ${RELEASE_FULL_NAME}.tar.gz trac@sapsailing.com:${FOLDER}
scp release-notes.txt trac@sapsailing.com:${FOLDER}
if [ "${RELEASE_NAME}" = "main" ]; then
  LINKING_FOLDER=releases/build-${RELEASE_TIMESTAMP}
  ssh trac@sapsailing.com "mkdir -p ${LINKING_FOLDER}; ln -s ../${RELEASE_FULL_NAME}/${RELEASE_FULL_NAME}.tar.gz ${LINKING_FOLDER}/build-${RELEASE_TIMESTAMP}.tar.gz; ln -s ../${RELEASE_FULL_NAME}/release-notes.txt ${LINKING_FOLDER}"
fi
rm ${RELEASE_FULL_NAME}.tar.gz release-notes.txt
