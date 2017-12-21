#!/bin/sh
export PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin

cd $(dirname "$0")/..

CP="lib/*"
CFG="cfg/db.config"
LOG="log/scrape.log"

java -cp "$CP" -Dconfig="$CFG" atsb.eve.dirt.DirtTaskDaemon $@ >> "$LOG" 2>&1
