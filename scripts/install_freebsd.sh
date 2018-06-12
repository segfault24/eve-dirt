#!/bin/sh
FQDN=dirt.lan
INSTALL_DIR=/srv/dirt
RUN_USER=dirt
WWW_USER=www

if ! [ $(id -u) = 0 ]; then
    echo "This script must be run with root privileges"
    exit 1
fi

# work from the root of the install directory
cd ${INSTALL_DIR}

# create the user if it doesn't exist
if ! id -u ${RUN_USER}; then
    pw useradd -q -n ${RUN_USER} -d ${INSTALL_DIR} -s /usr/sbin/nologin -w random
fi

# add the web server user to the group
pw usermod ${WWW_USER} -G ${RUN_USER}

# setup data directory
mkdir -p www/logs
chown -R ${RUN_USER}:${RUN_USER} ${INSTALL_DIR}
chmod -R o-rwx ${INSTALL_DIR}

# generate self signed cert
APACHE=/usr/local/etc/apache24
SUBJ="/C=US/ST=New\ York/L=New\ York/O=The\ Ether/CN=${FQDN}"
KEYOUT=${APACHE}/ssl/${FQDN}.key
CRTOUT=${APACHE}/ssl/${FQDN}.crt
mkdir -m 700 ${APACHE}/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout "${KEYOUT}" -out "${CRTOUT}" -subj "${SUBJ}"
chmod 400 ${KEYOUT} ${CRTOUT}

# add to apache
cp cfg/site.conf cfg/site.conf.tmp
sed -i '' "s/example.com/${FQDN}/g" cfg/site.conf.tmp
TEMP=$(echo ${APACHE} | sed "s/\//\\\\\//g")
sed -i '' "s/APACHEDIR/${TEMP}/g" cfg/site.conf.tmp
TEMP=$(echo ${INSTALL_DIR} | sed "s/\//\\\\\//g")
sed -i '' "s/INSTALLDIR/${TEMP}/g" cfg/site.conf.tmp
mv cfg/site.conf.tmp ${APACHE}/sites-enabled/dirt-${FQDN}.conf

# initialize db
mysql -u root -e "CREATE DATABASE eve;"
mysql -u root eve < sql/invTypes.sql
mysql -u root eve < sql/invMarketGroups.sql
mysql -u root eve < sql/mapRegions.sql
mysql -u root eve < sql/mapSolarSystems.sql
mysql -u root eve < sql/staStations.sql
mysql -u root eve < sql/dirt.sql

# cron job for daemon
cp cfg/example.cron cfg/cron.tmp
TEMP=$(echo ${INSTALL_DIR} | sed "s/\//\\\\\//g")
sed -i '' "s/INSTALLDIR/${TEMP}/g" cfg/cron.tmp
crontab -u ${RUN_USER} cfg/cron.tmp
rm cfg/cron.tmp

# restart apache
service apache24 restart

# sso instructions
echo Setup SSO integration:
echo 1\) Go to https://developers.eveonline.com/applications
echo 2\) Create New Application
echo 3\) Give it a name and description
echo 4\) Select Authentication \& API Access
echo 5\) Select the following permissions:
echo --a\) esi-wallet.read_character_wallet.v1
echo --b\) esi-markets.read_character_orders.v1
echo --c\) esi-assets.read_assets.v1
echo --d\) esi-universe.read_structures.v1
echo --e\) esi-markets.structure_markets.v1
echo --f\) esi-ui.open_window.v1
echo 6\) Enter the call back URL \(htps://${FQDN}/sso-auth/callback\)
echo 7\) Create Application
echo 8\) Copy the Client ID and Secret Key into:
echo --a\) ${INSTALL_DIR}/cfg/db.config
echo --b\) ${INSTALL_DIR}/www/classes/Site.php
