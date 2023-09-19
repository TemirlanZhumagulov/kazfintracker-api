#!/usr/bin/env bash

set -e

cd "$(dirname "$0")" || exit 113

echo -n "$(git rev-parse --abbrev-ref HEAD)" | awk '{print tolower($0)}'
