## Building
Prerequisites: *git*, *openjdk8*, *apache-ant*, and *php-composer*
```bash
# freebsd
pkg install git openjdk8 apache-ant
portsnap fetch extract
cd /usr/ports/devel/php-composer
make install clean
```

```bash
git clone https://github.com/segfault24/eve-dirt.git
cd eve-dirt
./build.sh
```

## Installing
1. Install Apache, MySQL, and PHP. There are a million different guides out there for all different operating systems and distributions. This guide only covers *nix systems, specifically Debian and FreeBSD.
```bash
# debian
apt-get install apache2 mysql-server mysql-client php php-mysql php-curl php-pear
a2enmod rewrite deflate
mysql_secure_installation
```
```bash
# freebsd
pkg install apache24 mod_php72 mysql57-server php72\
php72-ctype php72-curl php72-dom php72-filter php72-hash\
php72-iconv php72-json php72-mbstring php72-mysqli php72-openssl\
php72-pdo php72-pdo_mysql php72-phar php72-session php72-tokenizer\
php72-xmlwriter php72-zlib
mysql_secure_installation
```

2. Setup user and groups (as root, or with sudo)
```bash
useradd dirt
passwd dirt
usermod -G dirt www-data # add the apache user to the dirt group (mine was running as www-data)
```

3. Unpack archive into directory
```bash
mkdir -p -m 775 /srv/dirt
tar xzf eve-dirt.tar.gz -C /srv/dirt
mkdir -p -m 775 /srv/dirt/www/logs
chown -R dirt:dirt /srv/dirt
```

4. Configure Apache
    1. In the main configuration file:
    ```apacheconf
    <Directory "/srv/dirt/">
        Options FollowSymLinks
        AllowOverride All
        Require all granted
    </Directory>
    ```
    2. Create a vhost with:
    ```apacheconf
    DocumentRoot /srv/dirt/www/public/
    ```

5. Setup database and admin account from a privileged SQL prompt
```sql
CREATE DATABASE eve;
CREATE USER 'dirt.admin'@'localhost' IDENTIFIED BY 'pickapassword';
GRANT ALL PRIVILEGES ON eve.* TO 'dirt.admin'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

6. Load the tables
```bash
mysql -u root -p eve < sql/invTypes.sql
mysql -u root -p eve < sql/invMarketGroups.sql
mysql -u root -p eve < mapRegions.sql
mysql -u root -p eve < mapSolarSystems.sql
mysql -u root -p eve < staStations.sql
mysql -u root -p eve < sql/dirt.sql
```

7. Setup SSO application
    1. Go to https://developers.eveonline.com/applications
    2. **Create New Application**
    3. Give it a name and description
    4. Select **Authentication & API Access**
    5. Select the following permissions:
    ```
    esi-wallet.read_character_wallet.v1
    esi-markets.read_character_orders.v1
    esi-assets.read_assets.v1
    esi-universe.read_structures.v1
    esi-markets.structure_markets.v1
    esi-ui.open_window.v1
    ```
    6. Enter the call back URL (ex. https://localhost/sso-auth/callback)
    7. **Create Application**
    8. Copy the Client ID and Secret Key into *cfg/db.config* and *www/classes/Site.php*

8. Configure cron (sudo crontab -u dirt -e)
    * See *cfg/example.cron* for reference

## Maintenance Tasks
### Updating static data
1. run *bin/fetchLatestSde.sh*
2. run new SQL scripts
3. run *bin/genTopTypes.sh*

### New Monthly Economic Reports (MER)
1. 

