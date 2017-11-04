<?php
// Routes

$app->get('/', function ($request, $response, $args) {
	return $this->renderer->render($response, 'index.phtml', $args);
});

////////////////////////////////////////////////
////             Account Pages              ////
////////////////////////////////////////////////

$app->get('/login', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if($u->isLoggedIn()) {
		// redirect if already logged in
		return $response->withStatus(302)->withHeader('Location', '/dashboard');
	} else {
		return $this->renderer->render($response, 'login.phtml', $args);
	}
});

$app->post('/login', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if($u->login($request->getParsedBody()['username'], $request->getParsedBody()['password'])) {
		// successfully logged in
		return $response->withStatus(302)->withHeader('Location', '/dashboard');
	} else {
		$args['error'] = 'Incorrect username or password.';
		$this->logger->warning('/login unsuccessful login attempt for username: '.$request->getParsedBody()['username']);
		return $this->renderer->render($response, 'login.phtml', $args);
	}
});

$app->get('/logout', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	$u->logout();
	return $response->withStatus(302)->withHeader('Location', '/');
});

$app->get('/user-settings', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}
	$u->setTemplateVars($args);

	return $this->renderer->render($response, 'user-settings.phtml', $args);
});

$app->post('/sso-auth/link', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}

	// redirect to the sso login
	$state = uniqid();
	$_SESSION['sso_auth_state'] = $state;

	$sso_scope  = 'esi-assets.read_assets.v1';
	$sso_scope .= ' esi-wallet.read_character_wallet.v1';
	$sso_scope .= ' esi-markets.read_character_orders.v1';
	$sso_scope .= ' esi-markets.structure_markets.v1';
	$sso_scope .= ' esi-ui.open_window.v1';
	$sso_scope .= ' esi-universe.read_structures.v1';

	$auth_url = 'https://login.eveonline.com/oauth/authorize'
			.'?response_type=code'
			.'&redirect_uri='.urlencode('https://'.Dirt\Site::DOMAIN.'/sso-auth/callback')
			.'&client_id='.Dirt\Site::SSO_CLIENT_ID
			.'&scope='.$sso_scope
			.'&state='.$state;

	return $response->withStatus(302)->withHeader('Location', $auth_url);
});

$app->get('/sso-auth/callback', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}

	// make sure the returned state is what we initially set
	if(!isset($_SESSION['sso_auth_state']) || !$_SESSION['sso_auth_state']==$request->getQueryParam('state')) {
		$this->logger->error('/sso-auth/callback failed to verify pre auth state');
		return $response->withStatus(302)->withHeader('Location', '/user-settings');
	}

	// we're done with this
	unset($_SESSION['sso_auth_state']);

	// get the access & refresh tokens
	$header = 'Authorization: Basic '.base64_encode(Dirt\Site::SSO_CLIENT_ID.':'.Dirt\Site::SSO_SECRET_KEY);
	$fields = 'grant_type=authorization_code&code='.$request->getQueryParam('code');
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, 'https://login.eveonline.com/oauth/token');
	curl_setopt($ch, CURLOPT_USERAGENT, 'DIRT/0.1 ('.Dirt\Site::WEBMASTER.')');
	curl_setopt($ch, CURLOPT_HTTPHEADER, array($header));
	curl_setopt($ch, CURLOPT_POST, 2);
	curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
	curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
	$result = curl_exec($ch);
	if($result == false) {
		$this->logger->error('/sso-auth/callback failed to retrieve oauth token');
		return $response->withStatus(302)->withHeader('Location', '/user-settings');
	}
	curl_close($ch);
	$rsp = json_decode($result);
	if(!isset($rsp->access_token)) {
		$this->logger->error('/sso-auth/callback failed to parse oauth token');
		return $response->withStatus(302)->withHeader('Location', '/user-settings');
	}
	$access_token = $rsp->access_token;
	$token_expires = $rsp->expires_in;
	$refresh_token = $rsp->refresh_token;

	// get the character details
	$header = 'Authorization: Bearer '.$access_token;
	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, 'https://login.eveonline.com/oauth/verify');
	curl_setopt($ch, CURLOPT_USERAGENT, 'DIRT/0.1 ('.Dirt\Site::WEBMASTER.')');
	curl_setopt($ch, CURLOPT_HTTPHEADER, array($header));
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
	curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
	$result = curl_exec($ch);
	if($result == false) {
		$this->logger->error('/sso-auth/callback failed to retrieve character details');
		return $response->withStatus(302)->withHeader('Location', '/user-settings');
	}
	curl_close($ch);
	$rsp = json_decode($result);
	if(!isset($rsp->CharacterID)) {
		$this->logger->error('/sso-auth/callback failed to parse character details');
		return $response->withStatus(302)->withHeader('Location', '/user-settings');
	}

	$u = Dirt\User::getUser();
	$ret = $u->linkCharacter(
			$rsp->CharacterID,
			$rsp->CharacterOwnerHash,
			$rsp->CharacterName,
			$access_token,
			$token_expires,
			$refresh_token
	);

	if($ret) {
		$this->logger->info('/sso-auth/callback successfully linked character');
	} else {
		$this->logger->error('/sso-auth/callback failed to link character');
	}

	return $response->withStatus(302)->withHeader('Location', '/user-settings');
});

$app->post('/sso-auth/unlink', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}

	$u->unlinkCharacter($u->getActiveCharId());
	return $response->withStatus(302)->withHeader('Location', '/user-settings');
});

////////////////////////////////////////////////
////             Admin Pages                ////
////////////////////////////////////////////////

$app->get('/admin', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isAdmin()) {
		$this->logger->warning('/admin unauthorized access attempt');
		return $response->withStatus(302)->withHeader('Location', '/dashboard');
	}
	$u->setTemplateVars($args);

	return $this->renderer->render($response, 'admin/index.phtml', $args);
});

$app->get('/admin/test', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isAdmin()) {
		$this->logger->warning('/admin/test unauthorized access attempt');
		return $response->withStatus(302)->withHeader('Location', '/dashboard');
	}
	$u->setTemplateVars($args);

	return $this->renderer->render($response, 'admin/test.phtml', $args);
});

////////////////////////////////////////////////
////            General Pages               ////
////////////////////////////////////////////////

$app->get('/dashboard', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}
	$u->setTemplateVars($args);

	return $this->renderer->render($response, 'dashboard.phtml', $args);
});

$app->get('/search', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}
	$u->setTemplateVars($args);

	$q = '%'.$request->getQueryParams()['q'].'%';
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
	if($cnt>=100) {
		$cnt = '100+';
	}

	if($cnt==1) {
		return $response->withStatus(302)->withHeader('Location', '/browse?type='.htmlspecialchars($rows[0]['typeID']));
	} else {
		$args['query'] = $request->getQueryParams()['q'];
		$args['count'] = $cnt;
		$args['data'] = $rows;
		return $this->renderer->render($response, 'search.phtml', $args);
	}
});

$app->get('/appraisal', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}
	$u->setTemplateVars($args);

	return $this->renderer->render($response, 'appraisal.phtml', $args);
});

$app->post('/appraisal[/{appraisalid}]', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}
	$u->setTemplateVars($args);

	$raw = $request->getParsedBody()['rawpaste'];

	$appraisalid = uniqid();
	//$a[];
	$lines = explode("\n", $raw);
	foreach($lines as $line) {
		$parts = explode("\t", $line);
		//$a.push($appraisalid, $parts[0], $parts[2]);
	}

	//$sql = 'INSERT INTO appraisals (appraisalid, typeid, quantity) VALUES '.str_repeat('(?,?,?),', count($a)-3).'(?,?,?)';

	return $this->renderer->render($response, 'appraisal.phtml', $args);
});

$app->get('/my-lists', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}
	$u->setTemplateVars($args);

	return $this->renderer->render($response, 'my-lists.phtml', $args);
});

////////////////////////////////////////////////
////             Market Pages               ////
////////////////////////////////////////////////

$app->get('/browse', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}
	$u->setTemplateVars($args);

	return $this->renderer->render($response, 'browse.phtml', $args);
});

$app->get('/import', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}
	$u->setTemplateVars($args);

	return $this->renderer->render($response, 'import.phtml', $args);
});

$app->get('/station-trade', function ($request, $response, $args) {
	$u = Dirt\User::getUser();
	if(!$u->isLoggedIn()) {
		return $response->withStatus(302)->withHeader('Location', '/login');
	}
	$u->setTemplateVars($args);

	return $this->renderer->render($response, 'station-trade.phtml', $args);
});

////////////////////////////////////////////////
////        Economic Report Pages           ////
////////////////////////////////////////////////

$app->get('/economic-reports/regional-by-region', function ($request, $response, $args) {
	return $this->renderer->render($response, 'economic-reports/regional-by-region.phtml', $args);
});

$app->get('/economic-reports/regional-by-statistic', function ($request, $response, $args) {
	return $this->renderer->render($response, 'economic-reports/regional-by-statistic.phtml', $args);
});

$app->get('/economic-reports/regional-by-month', function ($request, $response, $args) {
	return $this->renderer->render($response, 'economic-reports/regional-by-month.phtml', $args);
});

$app->get('/economic-reports/mined-produced-destroyed', function ($request, $response, $args) {
	return $this->renderer->render($response, 'economic-reports/mined-produced-destroyed.phtml', $args);
});

$app->get('/economic-reports/velocity-of-isk', function ($request, $response, $args) {
	return $this->renderer->render($response, 'economic-reports/velocity-of-isk.phtml', $args);
});

$app->get('/economic-reports/money-supply', function ($request, $response, $args) {
	return $this->renderer->render($response, 'economic-reports/money-supply.phtml', $args);
});

$app->get('/economic-reports/isk-faucets-sinks', function ($request, $response, $args) {
	return $this->renderer->render($response, 'economic-reports/isk-faucets-sinks.phtml', $args);
});

$app->get('/kill-dump', function ($request, $response, $args) {
	return $this->renderer->render($response, 'kill-dump.phtml', $args);
});
