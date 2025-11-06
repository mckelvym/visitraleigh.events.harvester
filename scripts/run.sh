#!/bin/bash

version=1.2.0
echo "docker run registry.hub.docker.com/mckelvym/raleigh-events-rss-generator:${version}"
docker pull registry.hub.docker.com/mckelvym/raleigh-events-rss-generator:$version
docker run --rm --name=visit-raleigh-harvester \
  -v $(pwd)/../logs:/logs \
  -v $(pwd)/../../visitraleigh.events.rss:/data \
  registry.hub.docker.com/mckelvym/raleigh-events-rss-generator:$version /data/events.xml
