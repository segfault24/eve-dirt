<?php

namespace Dirt;

class Tools {

	const SSO_AUTH_URL = 'https://login.eveonline.com/oauth/authorize';
	const SSO_TOKEN_URL = 'https://login.eveonline.com/oauth/token';
	const SSO_VERIFY_URL = 'https://login.eveonline.com/oauth/verify';

	// sso application information
	const SSO_USERAGENT = 'DIRT/0.1 ('.Site::WEBMASTER.')';
	const SSO_CALLBACK_URI = 'https://'.Site::DOMAIN.'/ssocallback.php';
	const SSO_SCOPE = 'characterMarketOrdersRead esi-assets.read_assets.v1 esi-ui.open_window.v1 esi-wallet.read_character_wallet.v1';

	public static function paramToIntArray($param) {
		if($param=='') {
			return [];
		}

		$arr = explode(',', $param);
		$ret = [];
		foreach($arr as $key => $value) {
			array_push($ret, intval($value, 10));
		}
		return $ret;
	}

}
