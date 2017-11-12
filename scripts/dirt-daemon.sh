#!/bin/sh
export PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin
java -cp "/srv/dirt/lib/*" -Dconfig="/srv/dirt/cfg/db.config" atsb.eve.dirt.DirtTaskDaemon $@
