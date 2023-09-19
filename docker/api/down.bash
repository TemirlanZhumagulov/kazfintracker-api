#!/usr/bin/env bash

cd "$(dirname "$0")" || exit 172

docker-compose -f docker-compose-api.yml down
