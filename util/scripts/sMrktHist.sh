#!/bin/sh
PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin
export PATH
java -cp "/srv/dirt/lib/*" -Dconfig="/srv/dirt/cfg/db.config" atsb.eve.dirt.MarketHistoryScraper $@
