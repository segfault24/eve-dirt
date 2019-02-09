#!/bin/sh
cd $(dirname "$0")/..
base=`pwd`

working=$base/tmp/mer-unzip

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
mv $working/*/* $working
mv $working/*/*/* $working

# parse and import
./load-mer.sh 2000-01 "$base/mer/cfg/iskvolume.config"  "$working/IskVolume.csv"
./load-mer.sh 2000-01 "$base/mer/cfg/moneysupply.config" "$working/MoneySupply.csv"
./load-mer.sh 2000-01 "$base/mer/cfg/pdm.config" "$working/ProducedDestroyedMined.csv"
./load-mer.sh 2000-01 "$base/mer/cfg/regstat.config" "$working/RegionalStats.csv"

# clean up
rm -rf $working
