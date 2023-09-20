#!/usr/bin/env bash
set -e

cd "$(dirname "$0")" || exit 113

IMAGE_NAME=$(cat ./name.txt)
VERSION="$(cat ../version.txt)"

echo VERSION

bash ./image.bash

docker push "${IMAGE_NAME}"

docker tag "${IMAGE_NAME}" "${IMAGE_NAME}:${VERSION}"
docker push "${IMAGE_NAME}:${VERSION}"
echo "-------------------------------------------------------------------------"
echo "---  PUSHED DOCKER IMAGE ${IMAGE_NAME}:${VERSION}"
echo "-------------------------------------------------------------------------"
