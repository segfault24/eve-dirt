#!/bin/sh
#export PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin
cd $(dirname "$0")/../

cp="lib/eve-dirt.jar"
cp="$cp;lib/esi-client-20190330.jar"
cp="$cp;lib/esi-client-20190330-javadoc.jar"
cp="$cp;lib/eve-dirt.jar"
cp="$cp;lib/gson-2.6.2.jar"
cp="$cp;lib/hamcrest-core-1.3.jar"
cp="$cp;lib/joda-time-2.9.3.jar"
cp="$cp;lib/junit-4.12.jar"
cp="$cp;lib/log4j-api-2.11.2.jar"
cp="$cp;lib/log4j-core-2.11.2.jar"
cp="$cp;lib/logging-interceptor-2.7.5.jar"
cp="$cp;lib/mysql-connector-java-5.1.40-bin.jar"
cp="$cp;lib/okhttp-2.7.5.jar"
cp="$cp;lib/okio-1.6.0.jar"
cp="$cp;lib/swagger-annotations-1.5.12.jar"

java -cp "$cp" -Dconfig="cfg/db.ini" -Dlog4j.configurationFile="cfg/log4j2.xml" atsb.eve.dirt.DirtTaskDaemon --cli
