#!/bin/sh

cd ../sql

rm mapRegions.sql*
rm mapSolarSystems.sql*
rm staStations.sql*
rm invTypes.sql*
rm invMarketGroups.sql*

wget -N -P . https://www.fuzzwork.co.uk/dump/latest/mapRegions.sql.bz2
wget -N -P . https://www.fuzzwork.co.uk/dump/latest/mapSolarSystems.sql.bz2
wget -N -P . https://www.fuzzwork.co.uk/dump/latest/staStations.sql.bz2
wget -N -P . https://www.fuzzwork.co.uk/dump/latest/invTypes.sql.bz2
wget -N -P . https://www.fuzzwork.co.uk/dump/latest/invMarketGroups.sql.bz2

bzip2 -d *.bz2
