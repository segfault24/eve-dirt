<?php
use Dirt\Tools;

// //////////////////////////////////////////////
// // Contract Pages ////
// //////////////////////////////////////////////

$app->get('/contracts', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'contracts.phtml', $args);
});

$app->get('/contracts-finished', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
        ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);
    
    return $this->renderer->render($response, 'contracts-finished.phtml', $args);
});

$app->get('/contract', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'contract.phtml', $args);
});

$app->get('/doctrines', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
        ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    $db = Dirt\Database::getDb();
    $sql = 'SELECT l.name, s.structName, d.quantity, d.target
            FROM doctrine AS d
            JOIN dirtList AS l ON d.listId=l.listId
            JOIN structure AS s ON d.locationId=s.structId';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $args['doclist'] = $rows;

    return $this->renderer->render($response, 'doctrines.phtml', $args);
});