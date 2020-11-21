<?php
use Dirt\Tools;

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

    $response = $this->cache->denyCache($response);
    return $this->renderer->render($response, 'admin/index.phtml', $args);
});

$app->get('/admin/create-user', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isAdmin()) {
        $this->logger->warning('/admin/create-user unauthorized access attempt');
        return $response->withStatus(302)
        ->withHeader('Location', '/dashboard');
    }
    $u->setTemplateVars($args);

    $response = $this->cache->denyCache($response);
    return $this->renderer->render($response, 'admin/create-user.phtml', $args);
});

$app->post('/admin/create-user', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isAdmin()) {
        $this->logger->warning('/admin/create-user unauthorized access attempt');
        return $response->withStatus(302)
        ->withHeader('Location', '/dashboard');
    }

    $usernm = $request->getParsedBody()['usernm'];
    $userpw = $request->getParsedBody()['userpw'];
    $userpwconf = $request->getParsedBody()['userpwconf'];
    $admin = false;

    $err = Dirt\Tools::createUser($usernm, $userpw, $userpwconf, $admin);
    if (empty($err)) {
        $this->logger->info('/admin/create-user created user ' . $usernm);
        $args['successmsg'] = "Successfully created user";
    } else {
        $this->logger->info('/admin/create-user failed to create user: ' . $err);
        $args['errormsg'] = $err;
    }
    $u->setTemplateVars($args);

    $response = $this->cache->denyCache($response);
    return $this->renderer->render($response, 'admin/create-user.phtml', $args);
});

$app->get('/admin/list-users', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isAdmin()) {
        $this->logger->warning('/admin/list-users unauthorized access attempt');
        return $response->withStatus(302)
        ->withHeader('Location', '/dashboard');
    }
    $u->setTemplateVars($args);

    $db = Dirt\Database::getDb();
    $sql = 'SELECT userId, username, name, dateCreated, lastLogin, admin, disabled FROM dirtuser';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $args['userlist'] = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $response = $this->cache->denyCache($response);
    return $this->renderer->render($response, 'admin/list-users.phtml', $args);
});

$app->get('/admin/structure-auths', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isAdmin()) {
        $this->logger->warning('/admin/structure-auths unauthorized access attempt');
        return $response->withStatus(302)
        ->withHeader('Location', '/dashboard');
    }
    $u->setTemplateVars($args);

    $db = Dirt\Database::getDb();
    $sql = 'SELECT r.regionName, s.structName, a.charName, u.username
            FROM dirtstructauth AS d
            JOIN structure AS s ON d.structId=s.structId
            JOIN region AS r ON s.regionId=r.regionId
            JOIN dirtapiauth AS a ON d.keyId=a.keyId
            JOIN dirtuser AS u ON u.userId=a.userId
            ORDER BY r.regionName, s.structName';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $args['authlist'] = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $response = $this->cache->denyCache($response);
    return $this->renderer->render($response, 'admin/structure-auths.phtml', $args);
});
