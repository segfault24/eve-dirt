<?php

// //////////////////////////////////////////////
// // Notifications ////
// //////////////////////////////////////////////

$app->get('/api/notifications', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    // get unacknowledged notifications
    $db = Dirt\Database::getDb();
    $sql = 'SELECT `time`, title, text, typeId
            FROM dirtnotification
            WHERE userId=:userid
            AND acknowledged=0';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':userid' => $u->getUserId()
    ));

    $response = $this->cache->denyCache($response);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/notifications/new', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    // get unsent notifications
    $db = Dirt\Database::getDb();
    $sql = 'SELECT `time`, title, text, typeId
            FROM dirtnotification
            WHERE userId=:userid
            AND sent=0';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':userid' => $u->getUserId()
    ));
    $response = $this->cache->denyCache($response);
    $n = $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));

    // mark all as sent
    $stmtb = $db->prepare('UPDATE dirtnotification SET sent=1 WHERE userId=:userid');
    $stmtb->execute(array(
        ':userid' => $u->getUserId()
    ));

    return $n;
});

