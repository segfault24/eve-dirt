## Building
Prerequisites: *git*, *openjdk8*, *apache-ant*, and *php-composer*
```bash
# debian
apt-get install git openjdk-8-jdk ant composer
```
```bash
# freebsd
pkg install git openjdk8 apache-ant
portsnap fetch extract
cd /usr/ports/devel/php-composer
make install clean
```
```bash
# common
git clone https://github.com/segfault24/eve-dirt.git
cd eve-dirt && ant
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
php72-xmlwriter php72-zlib flock openjdk8-jre
mysql_secure_installation
```

2. Unpack archive into target directory
```bash
mkdir -p <install_dir>
tar xzf eve-dirt.tar.gz -C <install_dir>
```

3. Setup SSO application
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
    8. Copy the Client ID and Secret Key into the install script in bin/

4. Edit the values in the install script, then run it
```bash
cd <install_dir>/bin
vim install_freebsd.sh
sudo ./install_freebsd.sh
```

5. Update session.gc_maxlifetime in your php.ini to 86400

## Maintenance Tasks
### Updating static data
1. run *bin/fetchLatestSde.sh*
2. run new SQL scripts
3. run *bin/genTopTypes.sh*

### New Monthly Economic Reports (MER)
1. 

