## Building
Prerequisites: *openjdk*, *ant*, *ivy*, and *composer*
```bash
git clone https://github.com/segfault24/eve-dirt.git
cd eve-dirt
./build.sh
```

## Installing
1. Install Apache, MySQL, and PHP. There are a million different guides out there for all different operating systems and distributions. This guide only covers *nix systems, specifically Debian and FreeBSD.
```bash
apt-get install apache2 mysql-server mysql-client php php-mysql php-pear
a2enmod rewrite deflate
mysql_secure_installation
```

2. Setup user and groups (as root, or with sudo)
```bash
useradd dirt
passwd dirt
usermod -G dirt www-data # add the apache user to the dirt group (mine was running as www-data)
```
```bash
mkdir -p /srv/dirt
chown -R dirt:dirt /srv/dirt
chmod 775 /srv/dirt
```

3. Unpack archive into directory
```bash
tar xzf eve-dirt.tar.gz -C /srv/dirt
chown -R dirt:dirt /srv/dirt
```

4. Configure Apache

  a. In the main configuration file:
```
<Directory "/srv/dirt/">
    Options FollowSymLinks
    AllowOverride All
    Require all granted
</Directory>
```

  b. Create a vhost with:
```
DocumentRoot /srv/dirt/www/public/
```

5. Setup database and admin account from a privileged SQL prompt
```sql
CREATE DATABASE eve;
CREATE USER 'dirt.admin' @ 'localhost' IDENTIFIED BY 'pickapassword';
GRANT ALL PRIVILEGES ON eve.* TO 'dirt.admin' @ 'localhost' WITH GRANT OPTION;
```

6. Load the tables
```bash
mysql -u 'dirt.admin' -p eve < invTypes.sql
....
mysql -u 'dirt.admin' -p eve < dirt.sql
```

7. Setup SSO application

  a. Go to https://developers.eveonline.com/applications

  b. **Create New Application**

  c. Give it a name and description

  d. Select **Authentication & API Access**

  e. Select the following permissions:
```
esi-wallet.read_character_wallet.v1
esi-markets.read_character_orders.v1
esi-assets.read_assets.v1
esi-universe.read_structures.v1
esi-markets.structure_markets.v1
esi-ui.open_window.v1
```

  f. Enter the call back URL (ex. https://localhost/sso-auth/callback)

  g. **Create Application**

  h. Copy the Client ID and Secret Key into *cfg/db.config* and *www/src/Site.php*

8. Configure cron (sudo crontab -u dirt -e)

  See *cfg/example.cron* for reference

## Maintenance Tasks
### Updating static data
1. run *bin/fetchLatestSde.sh*

2. run new SQL scripts

3. run *bin/genTopTypes.sh*

### New Monthly Economic Reports (MER)
1. 

