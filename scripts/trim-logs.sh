#!/bin/sh
export PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin

cd $(dirname "$0")/..

LOG="log/scrape.log"

tail -n 10000 $LOG > $LOG.tmp
mv -f $LOG.tmp $LOG
