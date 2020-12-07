#!/bin/sh
cd $(dirname "$0")/../

cp="lib"
cp="$cp;lib/esi-client.jar"
cp="$cp;lib/eve-tools.jar"
cp="$cp;lib/eve-dirt.jar"
cp="$cp;lib/gson-2.6.2.jar"
cp="$cp;lib/hamcrest-core-1.3.jar"
cp="$cp;lib/joda-time-2.9.3.jar"
cp="$cp;lib/junit-4.12.jar"
cp="$cp;lib/log4j-api-2.11.2.jar"
cp="$cp;lib/log4j-core-2.11.2.jar"
cp="$cp;lib/logging-interceptor-2.7.5.jar"
cp="$cp;lib/mysql-connector-java-8.0.17.jar"
cp="$cp;lib/okhttp-2.7.5.jar"
cp="$cp;lib/okio-1.6.0.jar"
cp="$cp;lib/swagger-annotations-1.5.12.jar"

echo "pwd: $(pwd)"
echo "classpath: $cp"
echo ""

java -cp "$cp" -Dconfig="cfg/db.ini" atsb.eve.dirt.PriceCompare $@
