#!/bin/sh

cd `dirname "$0"`
base=`pwd`

# clean old builds
rm -rf deploy

# build EveTools
cd $base/src/EveTools
ant clean package

# build frontend
cd $base/src/Frontend
composer install

# setup deploy directory
cd $base
mkdir -p deploy
mkdir -p deploy/bin
mkdir -p deploy/config
mkdir -p deploy/lib
mkdir -p deploy/sql
mkdir -p deploy/www

# copy everything in
cp scripts/* deploy/bin/
cp config/* deploy/config/
cp sql/* deploy/sql/
cp src/lib/* deploy/lib/
cp src/EveTools/build/jar/*.jar deploy/lib/
cp -R src/Frontend/* deploy/www/

