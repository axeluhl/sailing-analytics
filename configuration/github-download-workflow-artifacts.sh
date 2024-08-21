#!/bin/bash
# Call with two arguments:
#  - the name of the branch for which you would like
#    to download the latest workflow run's build artifacts
#  - the Github PAT (personal access token)
# If found, the build.log.zip and test-result.zip files
# will be downloaded to the current working directory.
# The script will exit with status 0 if the workflow's
# conclusion was "success" and the artifacts downloaded
# fine. If the workflow's conclusion was not "success",
# an exit status of 1 is returned. If the downloads fail,
# an exit status of 2 is returned.
BRANCH="${1}"
BEARER_TOKEN="${2}"
UNIX_TIME=$( date +%s )
UNIX_DATE=$( date --iso-8601=second )
UNIX_TIME_YESTERDAY=$(( UNIX_TIME - 10*24*3600 )) # look back ten days in time, trying to catch even re-runs of older jobs
DATE_YESTERDAY=$( date --iso-8601=second -d @${UNIX_TIME_YESTERDAY} )
HEADERS_FILE=$( mktemp headersXXXXX )
NEXT_PAGE="https://api.github.com/repos/SAP/sailing-analytics/actions/runs?created=${DATE_YESTERDAY/+/%2B}..${UNIX_DATE/+/%2B}&per_page=100"
ARTIFACTS_JSON=""
# Now go through the pages as long as we have a non-empty NEXT_PAGE URL and no valid ARTIFACTS_JSON
while [ -z "${ARTIFACTS_JSON}" -a -n "${NEXT_PAGE}" ]; do
  echo "Trying page ${NEXT_PAGE} ..."
  # Get the artifacts URL of the last workflow run triggered by a branch push for ${BRANCH}:
  LAST_WORKFLOW_FOR_BRANCH=$( curl -D "${HEADERS_FILE}" --silent -L -H 'Authorization: Bearer '${BEARER_TOKEN} "${NEXT_PAGE}" 2>/dev/null | jq -r '.workflow_runs | map(select(.status == "completed" and .name == "release" and ((.head_branch | startswith("'${BRANCH}'")) or (.head_branch | startswith("releases/'${BRANCH}'"))))) | sort_by(.updated_at) | reverse | .[0]' )
  NEXT_PAGE=$( grep "^link: " "${HEADERS_FILE}" | sed -e 's/^.*<\([^>]*\)>; rel="next".*$/\1/' )
  ARTIFACTS_URL=$( echo "${LAST_WORKFLOW_FOR_BRANCH}" | jq -r '.artifacts_url' )
  CONCLUSION=$( echo "${LAST_WORKFLOW_FOR_BRANCH}" | jq -r '.conclusion' )
  ARTIFACTS_JSON=$( curl --silent -H 'Authorization: Bearer '${BEARER_TOKEN} "${ARTIFACTS_URL}" )
done
rm "${HEADERS_FILE}"
if [ -z "${ARTIFACTS_JSON}" ]; then
  echo "Workflow run or artifacts not found"
  exit 1
fi
BUILD_LOG_URL=$( echo "${ARTIFACTS_JSON}" | jq -r '.artifacts | map(select(.name == "build.log"))[0].archive_download_url' )
if [ -z "${BUILD_LOG_URL}" ]; then
  echo "build.log artifact not found"
  exit 2
fi
echo "Downloading build.log ZIP from ${BUILD_LOG_URL}"
curl --silent --output build.log.zip -L -H 'Authorization: Bearer '${BEARER_TOKEN} "${BUILD_LOG_URL}"
TEST_RESULTS_URL=$( echo "${ARTIFACTS_JSON}" | jq -r '.artifacts | map(select(.name == "test-results"))[0].archive_download_url' )
if [ -z "${TEST_RESULTS_URL}" ]; then
  echo "test-results artifact not found"
  exit 2
fi
echo "Downloading test-results ZIP from ${TEST_RESULTS_URL}"
curl --silent --output test-results.zip -L -H 'Authorization: Bearer '${BEARER_TOKEN} "${TEST_RESULTS_URL}"
if [ "${CONCLUSION}" != "success" ]; then
  exit 1
fi
