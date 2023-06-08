#!/usr/bin/env bash

cd "$(dirname "$0")" || exit 131

docker-compose down

docker run --rm -v "$HOME/volumes/sandbox/:/data" \
       busybox:1.28 \
       find /data -mindepth 1 -maxdepth 1 -exec \
       rm -rf {} \;

sudo mkdir -p "$HOME/volumes/sandbox/elasticsearch"
sudo chmod 777 -R "$HOME/volumes/sandbox/elasticsearch"

docker-compose up -d
# shellcheck disable=SC2181
if [ "$?" != "0" ] ; then
  echo "%%%"
  echo "%%% ERROR of : docker-compose up -d"
  echo "%%%"
  exit $?
fi

echo "%%%"
echo "%%% ГОТОВО"
echo "%%%"
