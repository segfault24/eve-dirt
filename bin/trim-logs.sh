#!/bin/sh
export PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin
cd $(dirname "$0")/..
base=`pwd`

logfile="$base/log/scrape.log"

tail -n 10000 $logfile > $logfile.tmp
mv -f $logfile.tmp $logfile
