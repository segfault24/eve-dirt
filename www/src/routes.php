<?php
use Dirt\Tools;

// Routes

$app->get('/', function ($request, $response, $args) {
    return $this->renderer->render($response, 'index.phtml', $args);
});

// //////////////////////////////////////////////
// // Account Pages ////
// //////////////////////////////////////////////

$app->get('/login', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if ($u->isLoggedIn()) {
        // redirect if already logged in
        return $response->withStatus(302)
            ->withHeader('Location', '/dashboard');
    } else {
        return $this->renderer->render($response, 'login.phtml', $args);
    }
});

$app->post('/login', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if ($u->login($request->getParsedBody()['username'], $request->getParsedBody()['password'])) {
        // successfully logged in
        return $response->withStatus(302)
            ->withHeader('Location', '/dashboard');
    } else {
        $args['error'] = 'Incorrect username or password.';
        $this->logger->error('/login unsuccessful login attempt for username:' . $request->getParsedBody()['username']);
        return $this->renderer->render($response, 'login.phtml', $args);
    }
});

$app->get('/logout', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    $u->logout();
    return $response->withStatus(302)
        ->withHeader('Location', '/');
});

$app->get('/user-settings', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    $uid = $u->getUserId();
    $db = Dirt\Database::getDb();
    $sql = 'SELECT `charId`, `charName`
			FROM dirtApiAuth
			WHERE `userId`=:uid
			ORDER BY `charName`';
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':uid', $uid);
    $stmt->execute();

    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $args['charlist'] = $rows;

    return $this->renderer->render($response, 'user-settings.phtml', $args);
});

$app->post('/user-settings', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    $u->setActiveChar($request->getParsedBody()['charId']);
    return $response->withStatus(302)
        ->withHeader('Location', '/user-settings');
});

$app->post('/sso-auth/link', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    // redirect to the sso login
    $state = uniqid();
    $_SESSION['sso_auth_state'] = $state;

    $sso_callback_uri = 'https://' . Tools::getProperty('domain') . '/sso-auth/callback';
    $sso_client_id = Dirt\Tools::getProperty('ssoclientid');
    $sso_scope = Dirt\Tools::getProperty('ssoscope');
    $auth_url = Dirt\Tools::SSO_AUTH_URL . '?response_type=code&redirect_uri=' . urlencode($sso_callback_uri) . '&client_id=' . $sso_client_id . '&scope=' . $sso_scope . '&state=' . $state;

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
            ->withHeader('Location', '/user-settings');
    }

    // we're done with this
    unset($_SESSION['sso_auth_state']);

    // get the access & refresh tokens
    $result = Tools::oauthToken($request->getQueryParam('code'));
    if ($result == false) {
        $this->logger->error('/sso-auth/callback failed to retrieve oauth token');
        return $response->withStatus(302)
            ->withHeader('Location', '/user-settings');
    }
    $rsp = json_decode($result);
    if (! isset($rsp->access_token)) {
        $this->logger->error('/sso-auth/callback failed to parse oauth token');
        return $response->withStatus(302)
            ->withHeader('Location', '/user-settings');
    }
    $access_token = $rsp->access_token;
    $token_expires = $rsp->expires_in;
    $refresh_token = $rsp->refresh_token;

    // get the character details
    $result = Tools::oauthVerify($access_token);
    if ($result == false) {
        $this->logger->error('/sso-auth/callback failed to retrieve character details');
        return $response->withStatus(302)
            ->withHeader('Location', '/user-settings');
    }
    $rsp = json_decode($result);
    if (! isset($rsp->CharacterID)) {
        $this->logger->error('/sso-auth/callback failed to parse character details');
        return $response->withStatus(302)
            ->withHeader('Location', '/user-settings');
    }

    $u = Dirt\User::getUser();
    $ret = $u->linkCharacter($rsp->CharacterID, $rsp->CharacterOwnerHash, $rsp->CharacterName, $access_token, $token_expires, $refresh_token);

    if ($ret) {
        $this->logger->info('/sso-auth/callback successfully linked character');
    } else {
        $this->logger->error('/sso-auth/callback failed to link character');
    }

    return $response->withStatus(302)
        ->withHeader('Location', '/user-settings');
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
        ->withHeader('Location', '/user-settings');
});

// //////////////////////////////////////////////
// // Admin Pages ////
// //////////////////////////////////////////////

$app->get('/admin', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isAdmin()) {
        $this->logger->warning('/admin unauthorized access attempt');
        return $response->withStatus(302)
            ->withHeader('Location', '/dashboard');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'admin/index.phtml', $args);
});

$app->get('/admin/test', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isAdmin()) {
        $this->logger->warning('/admin/test unauthorized access attempt');
        return $response->withStatus(302)
            ->withHeader('Location', '/dashboard');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'admin/test.phtml', $args);
});

// //////////////////////////////////////////////
// // General Pages ////
// //////////////////////////////////////////////

$app->get('/dashboard', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'dashboard.phtml', $args);
});

$app->get('/search', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    $q = '%' . $request->getQueryParams()['q'] . '%';
    $db = Dirt\Database::getDb();
    $sql = 'SELECT `typeID`, `typeName`
			FROM invTypes
			WHERE `typeName` LIKE :query
			AND `published`=1
			ORDER BY `typeName`
			LIMIT 100';
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':query', $q);
    $stmt->execute();

    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $cnt = count($rows);
    if ($cnt >= 100) {
        $cnt = '100+';
    }

    if ($cnt == 1) {
        return $response->withStatus(302)
            ->withHeader('Location', '/browse?type=' . htmlspecialchars($rows[0]['typeID']));
    } else {
        $args['query'] = $request->getQueryParams()['q'];
        $args['count'] = $cnt;
        $args['data'] = $rows;
        return $this->renderer->render($response, 'search.phtml', $args);
    }
});

$app->get('/appraisal', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'appraisal.phtml', $args);
});

$app->post('/appraisal[/{appraisalid}]', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    $raw = $request->getParsedBody()['rawpaste'];

    $appraisalid = uniqid();
    // $a[];
    $lines = explode("\n", $raw);
    foreach ($lines as $line) {
        $parts = explode("\t", $line);
        // $a.push($appraisalid, $parts[0], $parts[2]);
    }

    // $sql = 'INSERT INTO appraisals (appraisalid, typeid, quantity) VALUES '.str_repeat('(?,?,?),', count($a)-3).'(?,?,?)';

    return $this->renderer->render($response, 'appraisal.phtml', $args);
});

$app->get('/my-lists', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'my-lists.phtml', $args);
});

$app->get('/list-detail', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'list-detail.phtml', $args);
});

// //////////////////////////////////////////////
// // Market Pages ////
// //////////////////////////////////////////////

$app->get('/browse', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'browse.phtml', $args);
});

$app->get('/import', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'import.phtml', $args);
});

$app->get('/station-trade', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'station-trade.phtml', $args);
});

$app->get('/insurance', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'insurance.phtml', $args);
});

// //////////////////////////////////////////////
// // Economic Report Pages ////
// //////////////////////////////////////////////

$app->get('/economic-reports/regional-by-region', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'economic-reports/regional-by-region.phtml', $args);
});

$app->get('/economic-reports/regional-by-statistic', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'economic-reports/regional-by-statistic.phtml', $args);
});

$app->get('/economic-reports/regional-by-month', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'economic-reports/regional-by-month.phtml', $args);
});

$app->get('/economic-reports/mined-produced-destroyed', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'economic-reports/mined-produced-destroyed.phtml', $args);
});

$app->get('/economic-reports/velocity-of-isk', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'economic-reports/velocity-of-isk.phtml', $args);
});

$app->get('/economic-reports/money-supply', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'economic-reports/money-supply.phtml', $args);
});

$app->get('/economic-reports/isk-faucets-sinks', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'economic-reports/isk-faucets-sinks.phtml', $args);
});

$app->get('/kill-dump', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'kill-dump.phtml', $args);
});

$app->get('/fortizar-chain', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'fortizar-chain.phtml', $args);
});

