#!/bin/bash
echo $COMMIT_SHA
echo $GITHUB_TOKEN
echo $STATE
echo $DESCRIPTION
echo $TARGET_URL
curl "https://api.github.com/repos/ayarhlaine/status-check/statuses/$COMMIT_SHA" \
-H "Content-Type: application/json" \
-H "Authorization: token $GITHUB_TOKEN" \
-X POST -d "{\"state\": \"$STATE\", \"context\": \"continuous-integration/jenkins\", \"description\": \"$DESCRIPTION\", \"target_url\": \"$TARGET_URL\"}"
