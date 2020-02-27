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

$app->get('/notifications', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    $uid = $u->getUserId();
    $db = Dirt\Database::getDb();
    $sql = 'SELECT `notifId`, `time`, `title`, `text`, `acknowledged`
            FROM dirtNotification
            WHERE `userId`=:uid
            ORDER BY `time` DESC LIMIT 1000';
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':uid', $uid);
    $stmt->execute();

    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $args['notiflist'] = $rows;

    return $this->renderer->render($response, 'notifications.phtml', $args);
});

$app->post('/notifications', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    $uid = $u->getUserId();
    $db = Dirt\Database::getDb();
    $sql = 'UPDATE dirtNotification SET acknowledged=1 WHERE userId=:uid';
    $nid = $request->getParsedBody()['notifId'];
    if ($nid != "all") {
        $sql .= ' AND notifId=:nid';
    }
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':uid', $uid);
    if ($nid != "all") {
        $stmt->bindParam(':nid', $nid);
    }
    $stmt->execute();

    return $response->withStatus(302)
        ->withHeader('Location', '/notifications');
});

$app->get('/characters', function ($request, $response, $args) {
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

    return $this->renderer->render($response, 'characters.phtml', $args);
});

$app->post('/characters', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    $u->setActiveChar($request->getParsedBody()['charId']);
    $this->logger->info('/characters set active character ' . $u->getActiveCharId() . ' for user ' . $u->getUserId());
    return $response->withStatus(302)
        ->withHeader('Location', '/characters');
});

// structures associated with this account
//
// select d.dsaId, r.regionName, s.structName, a.charName
// from eve.dirtstructauth as d
// join eve.structure as s
//   on d.structId=s.structId
// join eve.dirtapiauth as a
//   on d.keyId=a.keyId
// join eve.region as r
//   on s.regionId=r.regionId
// where a.userId=1
// order by r.regionName, s.structName
