<?php
use Dirt\Tools;

// //////////////////////////////////////////////
// // Eve SSO Authentication ////
// //////////////////////////////////////////////

$app->post('/sso-auth/link', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    // redirect to the sso login
    $state = uniqid();
    $_SESSION['sso_auth_state'] = $state;

    $sso_callback_uri = 'http://' . Tools::getProperty('domain') . '/sso-auth/callback';
    $sso_client_id = Dirt\Tools::getProperty('ssoclientid');
    $sso_scope = Dirt\Tools::getProperty('ssoscope');
    $sso_scope2 = Dirt\Tools::getProperty('ssoscope2');
    $auth_url = Dirt\Tools::SSO_AUTH_URL . '?response_type=code&redirect_uri=' . urlencode($sso_callback_uri) . '&client_id=' . $sso_client_id . '&scope=' . $sso_scope . ' ' . $sso_scope2 . '&state=' . $state;

    return $response->withStatus(302)
        ->withHeader('Location', $auth_url);
});

$app->get('/sso-auth/callback', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    // make sure the returned state is what we initially set
    if (! isset($_SESSION['sso_auth_state']) || ! $_SESSION['sso_auth_state'] == $request->getQueryParam('state')) {
        $this->logger->error('/sso-auth/callback failed to verify pre auth state');
        return $response->withStatus(302)
            ->withHeader('Location', '/characters');
    }

    // we're done with this
    unset($_SESSION['sso_auth_state']);

    // get the access & refresh tokens
    $result = Tools::oauthToken($request->getQueryParam('code'));
    if ($result == false) {
        $this->logger->error('/sso-auth/callback failed to retrieve oauth token');
        return $response->withStatus(302)
            ->withHeader('Location', '/characters');
    }
    $rsp = json_decode($result);
    if (! isset($rsp->access_token)) {
        $this->logger->error('/sso-auth/callback failed to parse oauth token');
        return $response->withStatus(302)
            ->withHeader('Location', '/characters');
    }
    $access_token = $rsp->access_token;
    $token_expires = $rsp->expires_in;
    $refresh_token = $rsp->refresh_token;

    // get the character details
    $result = Tools::oauthVerify($access_token);
    if ($result == false) {
        $this->logger->error('/sso-auth/callback failed to retrieve character details');
        return $response->withStatus(302)
            ->withHeader('Location', '/characters');
    }
    $rsp = json_decode($result);
    if (! isset($rsp->CharacterID)) {
        $this->logger->error('/sso-auth/callback failed to parse character details');
        return $response->withStatus(302)
            ->withHeader('Location', '/characters');
    }

    $u = Dirt\User::getUser();
    $ret = $u->linkCharacter($rsp->CharacterID, $rsp->CharacterOwnerHash, $rsp->CharacterName, $access_token, $token_expires, $refresh_token);

    if ($ret) {
        $this->logger->info('/sso-auth/callback successfully linked character ' . $rsp->CharacterID . ' to user ' . $u->getUserId());
    } else {
        $this->logger->error('/sso-auth/callback failed to link character ' . $rsp->CharacterID . ' to user ' . $u->getUserId());
    }

    return $response->withStatus(302)
        ->withHeader('Location', '/characters');
});

$app->post('/sso-auth/unlink', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    // grab this before unlinking
    $refresh_token = $u->getRefreshToken();

    // do unlinking in database
    $ret = $u->unlinkCharacter($request->getParsedBody()['charId']);

    // send revoke to CCP
    if ($ret) {
        Tools::oauthRevoke($refresh_token); // don't really care if it works, that's CCP's problem
    }

    return $response->withStatus(302)
        ->withHeader('Location', '/characters');
});
