<?php
use Dirt\Tools;

$app->get('/', function ($request, $response, $args) {
    return $this->renderer->render($response, 'index.phtml', $args);
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
    $sql = 'SELECT `typeId`, `typeName`
			FROM invType
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
            ->withHeader('Location', '/browse?type=' . htmlspecialchars($rows[0]['typeId']));
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

$app->get('/wallet', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'wallet.phtml', $args);
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

$app->get('/my-alerts', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'my-alerts.phtml', $args);
});

$app->get('/economic-reports', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'mer.phtml', $args);
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
