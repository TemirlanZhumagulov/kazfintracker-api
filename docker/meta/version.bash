#!/usr/bin/env bash

cd "$(dirname "$0")" || exit 113

RETE=$(cat ../../version.txt)

echo -n "$RETE"
