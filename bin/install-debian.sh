#!/bin/sh
FQDN=
RUN_USER=dirt
WWW_USER=www-data
SSO_CLIENT_ID=
SSO_SECRET_KEY=

if [ -z ${FQDN} ]; then
    echo The Fully Qualified Domain Name \(FQDN\) of the website must be filled
    echo in at the top of this script \(ex. dirt.mywebsite.com\)
    exit 1
fi

if [ -z ${SSO_CLIENT_ID} ] || [ -z ${SSO_SECRET_KEY} ]; then
    echo You need to setup the application with CCP and then
    echo add the client id and secret to the top of this script
    echo
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
    echo 6\) Enter the call back URL \(https://${FQDN}/sso-auth/callback\)
    echo 7\) Create Application
    echo 8\) Copy the client id and secret key into this script
    exit 1
fi

if ! [ $(id -u) = 0 ]; then
    echo "This script must be run with root privileges"
    exit 1
fi

# work from the root of the install directory
cd $(dirname "$0")/..
INSTALL_DIR=$(pwd)

# install java
apt-get install openjdk-8-jre-headless

# create the user if it doesn't exist
if ! id -u ${RUN_USER}; then
    adduser --quiet --system --disabled-password --shell /usr/sbin/nologin --group ${RUN_USER}
fi

# add the web server user to the group
usermod -a -G ${RUN_USER} ${WWW_USER}

# setup data directory
install -d -o ${RUN_USER} -g ${RUN_USER} -m 770 /var/log/evedirt
chown -R ${RUN_USER}:${RUN_USER} ${INSTALL_DIR}
chmod -R o-rwx ${INSTALL_DIR}
mv www/public/htaccess www/public/.htaccess
chmod ug+x bin/*.sh

# generate self signed cert
APACHE=/etc/apache2
SUBJ="/C=US/ST=New\ York/L=New\ York/O=The\ Ether/CN=${FQDN}"
KEYOUT=${APACHE}/ssl/${FQDN}.key
CRTOUT=${APACHE}/ssl/${FQDN}.crt
mkdir -m 700 ${APACHE}/ssl
openssl req -newkey rsa:2048 -nodes -x509 -keyout "${KEYOUT}" -out "${CRTOUT}" -subj "${SUBJ}"
chmod 400 ${KEYOUT} ${CRTOUT}

# customize configuration files
DIRT_DB_PW=$(tr -cd '[:alnum:]' < /dev/urandom | fold -w30 | head -n1)
INSTALL_DIR_ESC=$(echo ${INSTALL_DIR} | sed "s/\//\\\\\//g")
APACHE_DIR_ESC=$(echo ${APACHE} | sed "s/\//\\\\\//g")
sed -i "s/DIRTDBPW/${DIRT_DB_PW}/g" sql/dirt.sql
sed -i "s/DIRTDBPW/${DIRT_DB_PW}/g" cfg/db.ini
sed -i "s/DOMAINNAME/${FQDN}/g" sql/dirt.sql
sed -i "s/DOMAINNAME/${FQDN}/g" cfg/site.conf
sed -i "s/INSTALLDIR/${INSTALL_DIR_ESC}/g" cfg/jobs.cron
sed -i "s/INSTALLDIR/${INSTALL_DIR_ESC}/g" cfg/site.conf
sed -i "s/APACHEDIR/${APACHE_DIR_ESC}/g" cfg/site.conf
sed -i "s/APPCLIENTID/${SSO_CLIENT_ID}/g" sql/dirt.sql
sed -i "s/APPSECRETKEY/${SSO_SECRET_KEY}/g" sql/dirt.sql

# add to apache
cp cfg/site.conf ${APACHE}/sites-available/dirt-${FQDN}.conf
a2ensite dirt-${FQDN}

# initialize db
mysql -u root eve < sql/dirt.sql

# install daemon service
sed -i "s/INSTALLDIR/${INSTALL_DIR_ESC}/g" cfg/evedirt.service
sed -i "s/RUNUSER/${RUN_USER}/g" cfg/evedirt.service
install -o root -g root -m 644 cfg/evedirt.service /lib/systemd/system/evedirt.service
systemctl enable evedirt
systemctl start evedirt

# install cron job for daemon
crontab -u ${RUN_USER} cfg/jobs.cron

# restart apache
systemctl restart apache2

