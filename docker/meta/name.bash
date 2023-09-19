#!/usr/bin/env bash

cd "$(dirname "$0")" || exit 113

IMAGE_NAME=$(cat ./name.txt);

echo "${IMAGE_NAME}-$(git rev-parse --abbrev-ref HEAD)" | awk '{print tolower($0)}'
