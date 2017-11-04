## SQL Initialization
run bin/fetchLatestSde.sh
CREATE DATABASE IF NOT EXISTS `eve`;
USE `eve`;
run all the sde sqls
run dirt.sql

## Change SQL passwords

## Import EVE Monthly Econ Reports
process MER csv files

##
  setup SSO application

## 
  update db config file


## Start scrapers
  install openjdk maven
  setup cron, using cfg/example.cron for reference

## Maintenance Tasks
  update sde
    regen top types
  new MER
