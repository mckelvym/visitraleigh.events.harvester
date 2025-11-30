#!/bin/bash

# Source version from version.sh
source "$(dirname "$0")/version.sh"
echo "docker run registry.hub.docker.com/mckelvym/raleigh-events-rss-generator:${VERSION}"
docker pull registry.hub.docker.com/mckelvym/raleigh-events-rss-generator:$VERSION
docker run --rm --name=visit-raleigh-harvester \
  -v $(pwd)/../logs:/logs \
  -v $(pwd)/../../visitraleigh.events.rss:/data \
  registry.hub.docker.com/mckelvym/raleigh-events-rss-generator:$VERSION /data/events.xml
