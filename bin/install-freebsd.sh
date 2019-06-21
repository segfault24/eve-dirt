#!/bin/sh
FQDN=
RUN_USER=
WWW_USER=www
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

# create the user if it doesn't exist
if ! id -u ${RUN_USER}; then
    pw useradd -q -n ${RUN_USER} -d ${INSTALL_DIR} -s /usr/sbin/nologin -w random
fi

# install java
pkg install openjdk8-jre

# add the web server user to the group
pw usermod ${WWW_USER} -G ${RUN_USER}

# setup data directory
install -d -o ${RUN_USER} -g ${RUN_USER} -m 770 /var/log/evedirt
chown -R ${RUN_USER}:${RUN_USER} ${INSTALL_DIR}
chmod -R o-rwx ${INSTALL_DIR}
install -o ${RUN_USER} -g ${RUN_USER} www/public/htaccess www/public/.htaccess
chmod ug+x bin/*.sh

# generate self signed cert
APACHE=/usr/local/etc/apache24
SUBJ="/C=US/ST=New\ York/L=New\ York/O=The\ Ether/CN=${FQDN}"
KEYOUT=${APACHE}/ssl/${FQDN}.key
CRTOUT=${APACHE}/ssl/${FQDN}.crt
mkdir -m 700 ${APACHE}/ssl
openssl req -x509 -nodes -days 3650 -newkey rsa:2048 -keyout "${KEYOUT}" -out "${CRTOUT}" -subj "${SUBJ}"
chmod 400 ${KEYOUT} ${CRTOUT}

# customize configuration files
DIRT_DB_PW=$(tr -cd '[:alnum:]' < /dev/urandom | fold -w30 | head -n1)
INSTALL_DIR_ESC=$(echo ${INSTALL_DIR} | sed "s/\//\\\\\//g")
APACHE_DIR_ESC=$(echo ${APACHE} | sed "s/\//\\\\\//g")

cp  cfg/evedirt.conf.template  cfg/evedirt.conf
cp  cfg/evedirt.cron.template  cfg/evedirt.cron
cp  cfg/db.ini.template     cfg/db.ini
cp  sql/dirt.sql.template   sql/dirt.sql

sed -i '' "s/DIRTDBPW/${DIRT_DB_PW}/g"          sql/dirt.sql
sed -i '' "s/DOMAINNAME/${FQDN}/g"              sql/dirt.sql
sed -i '' "s/APPCLIENTID/${SSO_CLIENT_ID}/g"    sql/dirt.sql
sed -i '' "s/APPSECRETKEY/${SSO_SECRET_KEY}/g"  sql/dirt.sql
sed -i '' "s/DOMAINNAME/${FQDN}/g"              cfg/evedirt.conf
sed -i '' "s/INSTALLDIR/${INSTALL_DIR_ESC}/g"   cfg/evedirt.conf
sed -i '' "s/APACHEDIR/${APACHE_DIR_ESC}/g"     cfg/evedirt.conf
sed -i '' "s/DIRTDBPW/${DIRT_DB_PW}/g"          cfg/db.ini
sed -i '' "s/INSTALLDIR/${INSTALL_DIR_ESC}/g"   cfg/evedirt.cron
sed -i '' "s/RUNUSER/${RUN_USER}/g"             cfg/evedirt.cron

# add to apache
cp cfg/evedirt.conf ${APACHE}/sites-enabled/evedirt.conf

# initialize db
mysql -u root eve < sql/dirt.sql

# install daemon service
cp cfg/evedirt.rc.template cfg/evedirt.rc
sed -i '' "s/INSTALLDIR/${INSTALL_DIR_ESC}/g"  cfg/evedirt.rc
sed -i '' "s/RUNUSER/${RUN_USER}/g"            cfg/evedirt.rc
install -o root -g wheel -m 555 cfg/evedirt.rc /usr/local/etc/rc.d/evedirt
sysrc evedirt_enable="YES"
service evedirt start

# install cron job for daemon
install -m 755 -o root -g wheel -d /usr/local/etc/cron.d
install -m 444 -o root -g wheel cfg/evedirt.cron /usr/local/etc/cron.d/evedirt.cron

# restart apache
service apache24 restart

# clean up
rm sql/dirt.sql
rm cfg/evedirt.conf
rm cfg/evedirt.rc
rm cfg/evedirt.cron

