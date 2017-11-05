## Building
install openjdk ivy composer
build.sh

## Installing
root shell
	apt-get install mysql-server mysql-client apache2 php php-mysql php-pear
	mysql_secure_installation

	useradd dirt
	passwd dirt
	usermod -G dirt www-data

	mkdir /srv/dirt
	chown dirt:dirt /srv/dirt
	chmod 775 /srv/dirt

move tar into /srv/dirt
	cd /srv/dirt
	tar xzvf eve-dirt.tar.gz


### MySQL
mysql -u yourusername -p eve < invTypes.sql

run bin/fetchLatestSde.sh
CREATE DATABASE IF NOT EXISTS `eve`;
USE `eve`;
run all the sde sqls
run dirt.sql

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
