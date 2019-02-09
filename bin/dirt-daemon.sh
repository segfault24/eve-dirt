#!/bin/sh
export PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin
cd $(dirname "$0")/..
base=`pwd`

classpath="$base/lib/*"
props="$base/cfg/db.ini"
logfile="/var/log/evedirt/scrape.log"

java -cp "$classpath" -Dconfig="$props" atsb.eve.dirt.DirtTaskDaemon $@ >> "$logfile" 2>&1
