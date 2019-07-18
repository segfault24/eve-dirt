<?php

// //////////////////////////////////////////////
// // Economic Report Data ////
// //////////////////////////////////////////////

$app->get('/api/economic-reports/mined-produced-destroyed', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT `date`, `produced`, `destroyed`, `mined` FROM merProdDestMine';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/economic-reports/velocity-of-isk', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT `date`, `iskVolume` AS volume FROM merIskVolume;';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/economic-reports/money-supply', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT `date`, `character`, `corporation`, `total` FROM merMoneySupply;';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/economic-reports/faucets-sinks/{year}/{month}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT `date`, `keyText`, `faucet`, `sink`, `sortValue` FROM merSinkFaucet WHERE YEAR(`date`)=:year AND MONTH(`date`)=:month ORDER BY `sortValue` ASC;';
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':year', $args['year']);
    $stmt->bindParam(':month', $args['month']);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/economic-reports/faucets-sinks', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT DISTINCT `date` FROM merSinkFaucet ORDER BY `date` DESC;';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

