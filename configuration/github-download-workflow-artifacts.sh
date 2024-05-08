#!/bin/bash
BRANCH="${1}"
BEARER_TOKEN="${2}"
# Find the last workflow run triggered by a branch push for ${BRANCH}:
curl -L -H 'Authorization: Bearer '${BEARER_TOKEN} https://api.github.com/repos/SAP/sailing-analytics/actions/runs 2>/dev/null | jq '.workflow_runs | map(select(.head_branch | startswith("'${BRANCH}'"))) | sort_by(.created_at) | reverse | .[0]'
