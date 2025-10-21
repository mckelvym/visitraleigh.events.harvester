#!/bin/bash

version=1.0.1
echo "docker run registry.hub.docker.com/mckelvym/raleigh-events-rss-generator:${version}"
docker pull registry.hub.docker.com/mckelvym/raleigh-events-rss-generator:$version
docker run --rm --name=visit-raleigh-harvester \
  -v $(pwd)/../../visitraleigh.events.rss:/data \
  registry.hub.docker.com/mckelvym/raleigh-events-rss-generator:$version /data/events.xml >> harvest.log
