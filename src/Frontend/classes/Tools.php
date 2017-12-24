<?php

namespace Dirt;

class Tools {

	const SSO_AUTH_URL   = 'https://login.eveonline.com/oauth/authorize';
	const SSO_TOKEN_URL  = 'https://login.eveonline.com/oauth/token';
	const SSO_VERIFY_URL = 'https://login.eveonline.com/oauth/verify';
	const SSO_REVOKE_URL = 'https://login.eveonline.com/oauth/revoke';

	// sso application information
	const SSO_USERAGENT = 'DIRT/0.1 ('.Site::WEBMASTER.')';
	const SSO_CALLBACK_URI = 'http://'.Site::DOMAIN.'/sso-auth/callback';
	const SSO_SCOPE =
	  'esi-wallet.read_character_wallet.v1'
    .' esi-universe.read_structures.v1'
    .' esi-assets.read_assets.v1'
    .' esi-ui.open_window.v1'
    .' esi-markets.structure_markets.v1'
    .' esi-markets.read_character_orders.v1';

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

	public static function oauthToken($auth_code) {
	    $header = 'Authorization: Basic '.base64_encode(Site::SSO_CLIENT_ID.':'.Site::SSO_SECRET_KEY);
	    $fields = 'grant_type=authorization_code&code='.$auth_code;
	    $ch = curl_init();
	    curl_setopt($ch, CURLOPT_URL, Tools::SSO_TOKEN_URL);
	    curl_setopt($ch, CURLOPT_USERAGENT, Tools::SSO_USERAGENT);
	    curl_setopt($ch, CURLOPT_HTTPHEADER, array($header));
	    curl_setopt($ch, CURLOPT_POST, 2);
	    curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
	    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
	    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
	    $result = curl_exec($ch);
	    curl_close($ch);

	    return $result;
	}

	public static function oauthVerify($access_token) {
	    $header = 'Authorization: Bearer '.$access_token;
	    $ch = curl_init();
	    curl_setopt($ch, CURLOPT_URL, Tools::SSO_VERIFY_URL);
	    curl_setopt($ch, CURLOPT_USERAGENT, Tools::SSO_USERAGENT);
	    curl_setopt($ch, CURLOPT_HTTPHEADER, array($header));
	    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
	    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
	    $result = curl_exec($ch);
	    curl_close($ch);

	    return $result;
	}

	public static function oauthRefresh() {
	    
	}

	public static function oauthRevoke($refresh_token) {
	    $header = 'Authorization: Basic '.base64_encode(Site::SSO_CLIENT_ID.':'.Site::SSO_SECRET_KEY);
	    $fields = 'token_type_hint=refresh_token&token='.$refresh_token;
	    $ch = curl_init();
	    curl_setopt($ch, CURLOPT_URL, Tools::SSO_REVOKE_URL);
	    curl_setopt($ch, CURLOPT_USERAGENT, Tools::SSO_USERAGENT);
	    curl_setopt($ch, CURLOPT_HTTPHEADER, array($header));
	    curl_setopt($ch, CURLOPT_POST, 2);
	    curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
	    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
	    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
	    $result = curl_exec($ch);
	    curl_close($ch);

	    return $result;
	}
}
