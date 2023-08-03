#!/usr/bin/env bash

REPO_OWNER="hylophile"
REPO_NAME="glove80-config"
WORKFLOW_NAME="build"
ARTIFACT_NAME="glove80.uf2"
ARTIFACT_PATH="."

gh_api() {
	gh api -H "Accept: application/vnd.github+json" -H "X-GitHub-Api-Version: 2022-11-28" $1
}

LATEST_ARTIFACT_ID=$(gh_api repos/$REPO_OWNER/$REPO_NAME/actions/artifacts | jq '.artifacts[0].id')

if cat .latest_artifact_id | grep $LATEST_ARTIFACT_ID; then
	echo "❌ already got the latest artifact. (wait a minute or 5)"
	exit 1
fi

echo $LATEST_ARTIFACT_ID >.latest_artifact_id

gh_api repos/$REPO_OWNER/$REPO_NAME/actions/artifacts/$LATEST_ARTIFACT_ID/zip >$ARTIFACT_NAME.zip

mv $ARTIFACT_NAME.zip old_$ARTIFACT_NAME.zip

unzip -q $ARTIFACT_NAME.zip -d $ARTIFACT_PATH

rm $ARTIFACT_NAME.zip

echo "✅ downloaded and extracted the most recent artifact."
