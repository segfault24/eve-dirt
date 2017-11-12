#!/bin/sh
export PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin
tail -n 10000 /srv/dirt/log/scrape.log > /srv/dirt/log/scrape.log.temp
mv -f /srv/dirt/log/scrape.log.temp /srv/dirt/log/scrape.log
