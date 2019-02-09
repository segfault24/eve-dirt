#!/bin/sh

cd $(dirname "$0")/..
base=`pwd`

if ! [ $(id -u) = 0 ]; then
    echo "This script must be run with root privileges"
    exit 1
fi

mysql -u root eve < sql/invTypes.sql
mysql -u root eve < sql/invMarketGroups.sql
mysql -u root eve < sql/mapRegions.sql
mysql -u root eve < sql/mapSolarSystems.sql
mysql -u root eve < sql/staStations.sql
