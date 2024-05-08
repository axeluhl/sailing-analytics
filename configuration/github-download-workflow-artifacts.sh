#!/bin/bash
BRANCH="${1}"
BEARER_TOKEN="${2}"
# Get the artifacts URL of the last workflow run triggered by a branch push for ${BRANCH}:
ARTIFACTS_URL=$( curl --silent -L -H 'Authorization: Bearer '${BEARER_TOKEN} https://api.github.com/repos/SAP/sailing-analytics/actions/runs 2>/dev/null | jq -r '.workflow_runs | map(select(.head_branch | startswith("'${BRANCH}'"))) | sort_by(.created_at) | reverse | .[0].artifacts_url' )
ARTIFACTS_JSON=$( curl --silent -H 'Authorization: Bearer '${BEARER_TOKEN} "${ARTIFACTS_URL}" )
if [ -z "${ARTIFACTS_JSON}" ]; then
  echo "Workflow run or artifacts not found"
  exit 1
fi
BUILD_LOG_URL=$( echo "${ARTIFACTS_JSON}" | jq -r '.artifacts | map(select(.name == "build.log"))[0].archive_download_url' )
if [ -z "${BUILD_LOG_URL}" ]; then
  echo "build.log artifact not found"
  exit 1
fi
echo "Downloading build.log ZIP from ${BUILD_LOG_URL}"
curl --silent --output build.log.zip -L -H 'Authorization: Bearer '${BEARER_TOKEN} "${BUILD_LOG_URL}"
TEST_RESULTS_URL=$( echo "${ARTIFACTS_JSON}" | jq -r '.artifacts | map(select(.name == "test-results"))[0].archive_download_url' )
if [ -z "${TEST_RESULTS_URL}" ]; then
  echo "test-results artifact not found"
  exit 1
fi
echo "Downloading test-results ZIP from ${TEST_RESULTS_URL}"
curl --silent --output test-results.zip -L -H 'Authorization: Bearer '${BEARER_TOKEN} "${TEST_RESULTS_URL}"

