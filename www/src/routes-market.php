<?php
use Dirt\Tools;

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

$app->get('/trade', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'trade.phtml', $args);
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

    $uid = $u->getUserId();
    $db = Dirt\Database::getDb();
    $sql = 'SELECT t.`typeName`, i.`name` AS tier, i.`cost`, i.`payout`
            FROM insuranceprice AS i
            JOIN invType AS t ON t.`typeId`=i.`typeId`
            ORDER BY t.`typeName`,i.`cost` DESC';
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':uid', $uid);
    $stmt->execute();

    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $args['inslist'] = $rows;

    return $this->renderer->render($response, 'insurance.phtml', $args);
});
