<?php

namespace Dirt;

class Site {

	//========================================
	// Site Settings
	//========================================
	const DOMAIN = 'dirt.local';
	const WEBMASTER = 'root@localhost';

	const MARKET_PUBLIC = false;
	const MAINTENANCE_MODE = false;

	//========================================
	// Database
	//========================================
	const DB_DRVR = 'mysql';
	const DB_ADDR = 'localhost';
	const DB_PORT = 3306;
	const DB_NAME = 'eve';
	const DB_USER = 'dirt.web';
	const DB_PASS = 'password';

	//========================================
	// Eve SSO OAuth2
	//========================================
	const SSO_CLIENT_ID = '';
	const SSO_SECRET_KEY = '';

}
