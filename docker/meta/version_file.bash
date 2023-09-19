#!/usr/bin/env bash

cd "$(dirname "$0")" || exit 113

VERSION_FILE=$(readlink -f ../../version.txt)

echo -n "$VERSION_FILE"
