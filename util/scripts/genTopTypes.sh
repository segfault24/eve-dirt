#!/bin/sh

java -cp "../lib/*" -Dconfig="../cfg/db.config" atsb.eve.dirt.TopTypes 1000 > ../cfg/top1000types.txt
java -cp "../lib/*" -Dconfig="../cfg/db.config" atsb.eve.dirt.TopTypes 1000000 > ../cfg/alltypes.txt
