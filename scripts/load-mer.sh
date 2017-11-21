#!/bin/sh
export PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin

working=/tmp/eve-dirt/mer-unzip

if [ "$#" -ne 1 ]; then
  echo "This script requires one argument, the location of the MER zip file to be imported"
  exit
fi

# clean any old stuff
# create working directory
rm -rf $working
mkdir -p $working

# extract contents
unzip -q $1 -d $working

# parse and import
java -cp "/srv/dirt/lib/*" -Dconfig="/srv/dirt/cfg/db.config" atsb.eve.dirt.mer.IskVolumeImporter "${working}/IskVolume.csv"
java -cp "/srv/dirt/lib/*" -Dconfig="/srv/dirt/cfg/db.config" atsb.eve.dirt.mer.MoneySupplyImporter "${working}/MoneySupply.csv"
java -cp "/srv/dirt/lib/*" -Dconfig="/srv/dirt/cfg/db.config" atsb.eve.dirt.mer.PDMImporter "${working}/ProducedDestroyedMined.csv"

# clean up
rm -rf $working
