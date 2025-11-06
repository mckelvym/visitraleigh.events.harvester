#!/bin/bash

cd ~/Documents/visitraleigh.events.rss
date=$(date "+%Y-%m-%d_%H.%M.%S")
size=$(du -hsc events.xml | head -n1 | cut -f1)
items=$(cat events.xml | grep "<item>" | wc -l)
git add events.xml && git commit -m "$date $size bytes, $items items" && git push

exit
