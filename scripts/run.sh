#!/bin/bash

# Source version from version.sh
source "$(dirname "$0")/version.sh"
echo "docker run registry.hub.docker.com/mckelvym/visitraleigh.events.harvester:${VERSION}"
docker pull registry.hub.docker.com/mckelvym/visitraleigh.events.harvester:$VERSION
docker run --rm --name=visit-raleigh-harvester \
  -v $(pwd)/../logs:/logs \
  -v $(pwd)/../../visitraleigh.events.rss:/data \
  registry.hub.docker.com/mckelvym/visitraleigh.events.harvester:$VERSION /data/events.xml
