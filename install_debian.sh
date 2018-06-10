#!/bin/sh
FQDN=dirt.lan
INSTALL_DIR=/srv/dirt
RUN_USER=dirt
WWW_GROUP=www-data

if ! [ $(id -u) = 0 ]; then
	echo "This script must be run with root privileges"
	exit 1
fi

DB_ROOT_PASSWORD=$(openssl rand -base64 16)
DB_ADMIN_PASSWORD=$(openssl rand -base64 16)

# create the user if it doesn't exist
if ! id -u ${RUN_USER}; then
	adduser ${RUN_USER} -q -d ${INSTALL_DIR} -D -G ${WWW_GROUP} -s /usr/sbin/nologin
fi

# setup data directory
mkdir -p -m 700 ${INSTALL_DIR}
tar xzf eve-dirt.tar.gz -C ${INSTALL_DIR}
mkdir -p -m 700 ${INSTALL_DIR}/www/logs
chown -R ${RUN_USER}:${RUN_USER} ${INSTALL_DIR}

# generate self signed cert
APACHE=/etc/apache2
SUBJ="/C=US/ST=New\ York/L=New\ York/O=The\ Ether/CN=${FQDN}"
KEYOUT=${APACHE}/ssl/${FQDN}.key
CRTOUT=${APACHE}/ssl/${FQDN}.crt
mkdir -m 700 ${APACHE}/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ${KEYOUT} -out ${CRTOUT} -subj ${SUBJ}
chmod 400 ${KEYOUT} ${CRTOUT}

# add to apache
cp ${INSTALL_DIR}/cfg/site.conf /tmp/temp.conf
sed -i '' "s/example.com/${FQDN}/g" /tmp/temp.conf
TEMP=$(echo ${APACHE} | sed "s/\//\\\\\//g")
sed -i '' "s/APACHEDIR/${TEMP}/g" /tmp/temp.conf
TEMP=$(echo ${INSTALL_DIR} | sed "s/\//\\\\\//g")
sed -i '' "s/INSTALLDIR/${TEMP}/g" /tmp/temp.conf
mv /tmp/temp.conf ${APACHE}/sites-enabled/dirt-${FQDN}.conf

# initialize db
mysql -u root -e "UPDATE mysql.user SET Password=PASSWORD('${DB_ROOT_PASSWORD}') WHERE User='root';"
mysql -u root -e "CREATE DATABASE eve;"
mysql -u root -e "CREATE USER 'dirt.admin'@'localhost' IDENTIFIED BY '${DB_ADMIN_PASSWORD}';"
mysql -u root -e "GRANT ALL PRIVILEGES ON eve.* TO 'dirt.admin'@'localhost' WITH GRANT OPTION;"
#mysql -u root -p eve < ${INSTALL_DIR}/sql/invTypes.sql
#mysql -u root -p eve < ${INSTALL_DIR}/sql/dirt.sql

# cron job for daemon
cp ${INSTALL_DIR}/cfg/exmaple.cron /tmp/temp.cron
TEMP=$(echo ${INSTALL_DIR} | sed "s/\//\\\\\//g")
sed -i '' "s/INSTALLDIR/${TEMP}/g" /tmp/temp.cron
crontab -u ${RUN_USER} /tmp/temp.cron
rm /tmp/temp.cron

# restart apache
systemctl restart apache2

# save the db password(s)
echo ${DB_ROOT_PASSWORD} > /root/db_passwords.txt
echo ${DB_ADMIN_PASSWORD} >> /root/db_passwords.txt
echo See /root/db_passwords.txt for DB credentials

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
