#!/bin/sh

if ! [ $(id -u) = 0 ]; then
    echo "This script must be run with root privileges"
    exit 1
fi

service evedirt stop
sysrc -x evedirt_enable

rm -f /usr/local/etc/cron.d/evedirt.cron
rm -f /usr/local/etc/rc.d/evedirt
rm -f /usr/local/etc/apache24/sites-enabled/evedirt.conf
rm -rf /var/log/evedirt
rm -rf /var/run/evedirt

service apache24 reload

