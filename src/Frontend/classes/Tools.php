<?php
namespace Dirt;

use PDO;

class Tools
{

    const SSO_AUTH_URL = 'https://login.eveonline.com/oauth/authorize';

    const SSO_TOKEN_URL = 'https://login.eveonline.com/oauth/token';

    const SSO_VERIFY_URL = 'https://login.eveonline.com/oauth/verify';

    const SSO_REVOKE_URL = 'https://login.eveonline.com/oauth/revoke';

    public static function paramToIntArray($param)
    {
        if ($param == '') {
            return [];
        }

        $arr = explode(',', $param);
        $ret = [];
        foreach ($arr as $key => $value) {
            array_push($ret, intval($value, 10));
        }
        return $ret;
    }

    public static function oauthToken($auth_code)
    {
        $sso_client_id = Tools::getProperty('ssoclientid');
        $sso_secret_key = Tools::getProperty('ssosecretkey');
        $user_agent = Tools::getProperty('useragent');
        $header = 'Authorization: Basic ' . base64_encode($sso_client_id . ':' . $sso_secret_key);
        $fields = 'grant_type=authorization_code&code=' . $auth_code;
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, Tools::SSO_TOKEN_URL);
        curl_setopt($ch, CURLOPT_USERAGENT, $user_agent);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array(
            $header
        ));
        curl_setopt($ch, CURLOPT_POST, 2);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
        $result = curl_exec($ch);
        curl_close($ch);

        return $result;
    }

    public static function oauthVerify($access_token)
    {
        $user_agent = Tools::getProperty('useragent');
        $header = 'Authorization: Bearer ' . $access_token;
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, Tools::SSO_VERIFY_URL);
        curl_setopt($ch, CURLOPT_USERAGENT, $user_agent);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array(
            $header
        ));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
        $result = curl_exec($ch);
        curl_close($ch);

        return $result;
    }

    public static function oauthRefresh($refresh_token)
    {
        $sso_client_id = Tools::getProperty('ssoclientid');
        $sso_secret_key = Tools::getProperty('ssosecretkey');
        $user_agent = Tools::getProperty('useragent');
        $header = 'Authorization: Basic ' . base64_encode($sso_client_id . ':' . $sso_secret_key);
        $fields = 'grant_type=refresh_token&refresh_token=' . $refresh_token;
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, Tools::SSO_TOKEN_URL);
        curl_setopt($ch, CURLOPT_USERAGENT, $user_agent);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array(
            $header
        ));
        curl_setopt($ch, CURLOPT_POST, 2);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
        $result = curl_exec($ch);
        curl_close($ch);

        return $result;
    }

    public static function oauthRevoke($refresh_token)
    {
        $sso_client_id = Tools::getProperty('ssoclientid');
        $sso_secret_key = Tools::getProperty('ssosecretkey');
        $user_agent = Tools::getProperty('useragent');
        $header = 'Authorization: Basic ' . base64_encode($sso_client_id . ':' . $sso_secret_key);
        $fields = 'token_type_hint=refresh_token&token=' . $refresh_token;
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, Tools::SSO_REVOKE_URL);
        curl_setopt($ch, CURLOPT_USERAGENT, $user_agent);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array(
            $header
        ));
        curl_setopt($ch, CURLOPT_POST, 2);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
        $result = curl_exec($ch);
        curl_close($ch);

        return $result;
    }

    public static function getProperty($property_name)
    {
        $db = Database::getDb();

        // get the user's info from the db
        $sql = 'SELECT `propertyValue` FROM property WHERE `propertyName`=:propname';
        $stmt = $db->prepare($sql);
        $stmt->bindParam(':propname', $property_name);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($row) {
            return $row['propertyValue'];
        } else {
            return null;
        }
    }
}
