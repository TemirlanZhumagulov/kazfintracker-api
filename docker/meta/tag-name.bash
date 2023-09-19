#!/usr/bin/env bash

#set -e

cd "$(dirname "$0")" || exit 113

VERSION_FILE=$(bash version_file.bash)
BRANCH="$(bash branch.bash)"

VERSION=$(echo -n "$(cat "$VERSION_FILE")")

TAG_NAME="snapshot-$BRANCH-$VERSION"

echo -n "$TAG_NAME"
