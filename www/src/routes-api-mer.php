<?php

// //////////////////////////////////////////////
// // Economic Report Data ////
// //////////////////////////////////////////////

$app->get('/api/economic-reports/mined-produced-destroyed', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT `date`, `produced`, `destroyed`, `mined` FROM merproddestmine';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 3600);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/economic-reports/velocity-of-isk', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT `date`, `iskVolume` AS volume FROM meriskvolume;';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 3600);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/economic-reports/money-supply', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT `date`, `character`, `corporation`, `total` FROM mermoneysupply;';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 3600);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/economic-reports/faucets-sinks/{year}/{month}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT `date`, `keyText`, `faucet`, `sink`, `sortValue` FROM mersinkfaucet WHERE YEAR(`date`)=:year AND MONTH(`date`)=:month ORDER BY `sortValue` ASC;';
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':year', $args['year']);
    $stmt->bindParam(':month', $args['month']);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 3600);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/economic-reports/faucets-sinks', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT DISTINCT `date` FROM mersinkfaucet ORDER BY `date` DESC;';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 3600);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

