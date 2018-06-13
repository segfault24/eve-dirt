<?php

namespace Dirt;

class Site {

	//========================================
	// Site Settings
	//========================================
	const DOMAIN = 'DOMAINNAME';
	const WEBMASTER = 'webmaster@DOMAINNAME';

	const MARKET_PUBLIC = false;
	const MAINTENANCE_MODE = false;

	//========================================
	// Database
	//========================================
	const DB_DRVR = 'mysql';
	const DB_ADDR = 'localhost';
	const DB_PORT = 3306;
	const DB_NAME = 'eve';
	const DB_USER = 'dirt';
	const DB_PASS = 'DIRTDBPW';

	//========================================
	// Eve SSO OAuth2
	//========================================
	const SSO_CLIENT_ID = 'APPCLIENTID';
	const SSO_SECRET_KEY = 'APPSECRETKEY';

}
