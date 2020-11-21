<?php
use Dirt\Tools;

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
        $response = $this->cache->denyCache($response);
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

$app->get('/user/notifications', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    $uid = $u->getUserId();
    $db = Dirt\Database::getDb();
    $sql = 'SELECT `notifId`, `time`, `title`, `text`, `acknowledged`
            FROM dirtnotification
            WHERE `userId`=:uid
            ORDER BY `time` DESC LIMIT 1000';
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':uid', $uid);
    $stmt->execute();

    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $args['notiflist'] = $rows;

    $response = $this->cache->denyCache($response);
    return $this->renderer->render($response, 'user/notifications.phtml', $args);
});

$app->post('/user/notifications', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    $uid = $u->getUserId();
    $db = Dirt\Database::getDb();
    $nid = $request->getParsedBody()['notifId'];
    if ($nid == "del-all") {
        // delete all
        $sql = 'DELETE FROM dirtnotification WHERE userId=:uid';
        $stmt = $db->prepare($sql);
        $stmt->bindParam(':uid', $uid);
        $stmt->execute();
    } else if ($nid == "ack-all") {
        // ack all
        $sql = 'UPDATE dirtnotification SET acknowledged=1 WHERE userId=:uid';
        $stmt = $db->prepare($sql);
        $stmt->bindParam(':uid', $uid);
        $stmt->execute();
    } else {
        // ack specific
        $sql = 'UPDATE dirtnotification SET acknowledged=1 WHERE userId=:uid AND notifId=:nid';
        $stmt = $db->prepare($sql);
        $stmt->bindParam(':uid', $uid);
        $stmt->bindParam(':nid', $nid);
        $stmt->execute();
    }

    return $response->withStatus(302)
        ->withHeader('Location', '/user/notifications');
});

$app->get('/user/characters', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    $uid = $u->getUserId();
    $db = Dirt\Database::getDb();
    $sql = 'SELECT `charId`, `charName`
            FROM dirtapiauth
            WHERE `userId`=:uid
            ORDER BY `charName`';
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':uid', $uid);
    $stmt->execute();

    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $args['charlist'] = $rows;

    $response = $this->cache->denyCache($response);
    return $this->renderer->render($response, 'user/characters.phtml', $args);
});

$app->post('/user/characters', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    $u->setActiveChar($request->getParsedBody()['charId']);
    $this->logger->info('/user/characters set active character ' . $u->getActiveCharId() . ' for user ' . $u->getUserId());
    return $response->withStatus(302)
        ->withHeader('Location', '/user/characters');
});

$app->get('/user/change-password', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
        ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    $response = $this->cache->denyCache($response);
    return $this->renderer->render($response, 'user/change-password.phtml', $args);
});

$app->post('/user/change-password', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
        ->withHeader('Location', '/login');
    }

    $oldpw = $request->getParsedBody()['userpw'];
    $newpw = $request->getParsedBody()['userpwnew'];
    $newpwconf = $request->getParsedBody()['userpwnewconf'];
    $err = $u->changePassword($oldpw, $newpw, $newpwconf);
    if (empty($err)) {
        $this->logger->info('/user/change-password changed password for user ' . $u->getUserId());
        $args['successmsg'] = "Successfully changed password";
    } else {
        $this->logger->info('/user/change-password failed to changed password for user ' . $u->getUserId() . ' ' . $err);
        $args['errormsg'] = $err;
    }
    $u->setTemplateVars($args);

    $response = $this->cache->denyCache($response);
    return $this->renderer->render($response, 'user/change-password.phtml', $args);
});
