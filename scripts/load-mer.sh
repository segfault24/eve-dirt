#!/bin/sh
cd $(dirname "$0")/..
base=`pwd`

classpath="$base/lib/*"
props="$base/cfg/db.ini"

java -cp "$classpath" -Dconfig="$props" atsb.eve.dirt.mer.MERLoader $@
