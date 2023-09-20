#!/usr/bin/env bash

set -e

PROJECT_NAME=api

IMAGE_NAME=$(cat ./name.txt)

cd ..

./gradlew clean bootJar

cd docker

docker build --tag "${IMAGE_NAME}" --build-arg projectName=${PROJECT_NAME} .
