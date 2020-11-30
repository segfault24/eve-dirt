<?php

// //////////////////////////////////////////////
// // Contract Data ////
// //////////////////////////////////////////////

$app->get('/api/contract/corp/exchange', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT co.`contractId`, locs.`locationName`, ich.`name` AS issuerName, co.`price`, co.`title`, co.`dateIssued`
            FROM corpcontract AS co
            LEFT JOIN dentity AS ich ON co.issuerId=ich.id
            LEFT JOIN dlocation AS locs ON co.`startLocationId`=locs.`locationId`
            WHERE co.`type`=2 AND co.`status`=1 AND `dateExpired`>NOW()
            ORDER BY co.dateIssued DESC';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/corp/exchange/finished', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT co.`contractId`, locs.`locationName`, ich.`name` AS issuerName, ach.`name` AS acceptorName, co.`price`, co.`title`, co.`dateCompleted`
            FROM corpcontract AS co
            LEFT JOIN dentity AS ich ON co.issuerId=ich.id
            LEFT JOIN dentity AS ach ON co.acceptorId=ach.id
            LEFT JOIN dlocation AS locs ON co.`startLocationId`=locs.`locationId`
            WHERE co.`type`=2 AND co.`status`=5
            ORDER BY co.dateIssued DESC LIMIT 1000';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/corp/courier', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT co.`contractId`, ich.`name` AS issuerName, co.`dateIssued`, slocs.`locationName` AS startLocation, elocs.`locationName` AS endLocation, co.`collateral`, co.`volume`, co.`reward`, co.`daysToComplete`
            FROM corpcontract AS co
            LEFT JOIN dentity AS ich ON co.issuerId=ich.id
            LEFT JOIN dlocation AS slocs ON co.`startLocationId`=slocs.`locationId`
            LEFT JOIN dlocation AS elocs ON co.`endLocationId`=elocs.`locationId`
            WHERE co.`type`=4 AND co.`status`=1 AND `dateExpired`>NOW()
            ORDER BY co.dateIssued DESC';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/corp/courier/in-progress', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT co.`contractId`, ich.`name` AS issuerName, co.`dateIssued`, slocs.`locationName` AS startLocation, elocs.`locationName` AS endLocation, co.`collateral`, co.`volume`, co.`reward`, ach.`name` AS acceptor
            FROM corpcontract AS co
            LEFT JOIN dentity AS ich ON co.issuerId=ich.id
            LEFT JOIN dentity AS ach ON co.acceptorId=ach.id
            LEFT JOIN dlocation AS slocs ON co.`startLocationId`=slocs.`locationId`
            LEFT JOIN dlocation AS elocs ON co.`endLocationId`=elocs.`locationId`
            WHERE co.`type`=4 AND co.`status`=2
            ORDER BY co.dateCompleted DESC';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/corp/courier/finished', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT co.`contractId`, ich.`name` AS issuerName, co.`dateIssued`, slocs.`locationName` AS startLocation, elocs.`locationName` AS endLocation,
                co.`collateral`, co.`volume`, co.`reward`, ach.`name` AS acceptor, co.`dateCompleted`
            FROM corpcontract AS co
            LEFT JOIN dentity AS ich ON co.issuerId=ich.id
            LEFT JOIN dentity AS ach ON co.acceptorId=ach.id
            LEFT JOIN dlocation AS slocs ON co.`startLocationId`=slocs.`locationId`
            LEFT JOIN dlocation AS elocs ON co.`endLocationId`=elocs.`locationId`
            WHERE co.`type`=4 AND co.`status`=5
            ORDER BY co.dateCompleted DESC LIMIT 1000';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/corp/courier/failed', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT co.`contractId`, ich.`name` AS issuerName, co.`dateIssued`, slocs.`locationName` AS startLocation, elocs.`locationName` AS endLocation,
                co.`collateral`, co.`volume`, co.`reward`, ach.`name` AS acceptor, co.`dateCompleted`
            FROM corpcontract AS co
            LEFT JOIN dentity AS ich ON co.issuerId=ich.id
            LEFT JOIN dentity AS ach ON co.acceptorId=ach.id
            LEFT JOIN dlocation AS slocs ON co.`startLocationId`=slocs.`locationId`
            LEFT JOIN dlocation AS elocs ON co.`endLocationId`=elocs.`locationId`
            WHERE co.`type`=4 AND co.`status`=8
            ORDER BY co.dateCompleted DESC LIMIT 1000';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/capital/outstanding', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT c.contractId, i.typeName, c.dateIssued, c.price, a.appraisal AS fittings, c.price - a.appraisal AS hullvalue, l.locationName, e.name AS issuer, c.title
            FROM corpcontract AS c
            JOIN corpcontractitem AS ci ON c.contractId=ci.contractId
            JOIN invtype AS i ON ci.typeId=i.typeId
            LEFT JOIN (
                SELECT c.contractId, SUM(j.best*ci.quantity) AS appraisal
                FROM corpcontract AS c
                JOIN corpcontractitem AS ci ON ci.contractId=c.contractId
                JOIN vjitabestsell AS j ON j.typeid=ci.typeid
                WHERE ci.typeId NOT IN (23911,24483,23757,23915,19722,19726,19720,19724,37605,37604,22852,23913,23917,23919,28352,3514,42125,45647,42243,42124,52907,42242,45645,37607,37606,11567,3764,671,23773,45649,42241,42126)
                GROUP BY c.contractId
            ) AS a ON a.contractId=c.contractId
            LEFT JOIN dlocation AS l ON l.locationId=c.startLocationId
            LEFT JOIN dentity AS e ON e.id=c.issuerId
            WHERE ci.typeId IN (23911,24483,23757,23915,19722,19726,19720,19724,37605,37604,22852,23913,23917,23919,28352,3514,42125,45647,42243,42124,52907,42242,45645,37607,37606,11567,3764,671,23773,45649,42241,42126)
            AND c.`type`=2 AND c.`status`=1 AND c.`dateExpired`>NOW()';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/capital/finished', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT c.contractId, i.typeName, c.dateCompleted, c.price, a.appraisal AS fittings, c.price - a.appraisal AS hullvalue, l.locationName, e.name AS issuer, c.title
            FROM corpcontract AS c
            JOIN corpcontractitem AS ci ON c.contractId=ci.contractId
            JOIN invtype AS i ON ci.typeId=i.typeId
            LEFT JOIN (
                SELECT c.contractId, SUM(j.best*ci.quantity) AS appraisal
                FROM corpcontract AS c
                JOIN corpcontractitem AS ci ON ci.contractId=c.contractId
                JOIN vjitabestsell AS j ON j.typeid=ci.typeid
                WHERE ci.typeId NOT IN (23911,24483,23757,23915,19722,19726,19720,19724,37605,37604,22852,23913,23917,23919,28352,3514,42125,45647,42243,42124,52907,42242,45645,37607,37606,11567,3764,671,23773,45649,42241,42126)
                GROUP BY c.contractId
            ) AS a ON a.contractId=c.contractId
            LEFT JOIN dlocation AS l ON l.locationId=c.startLocationId
            LEFT JOIN dentity AS e ON e.id=c.issuerId
            WHERE ci.typeId IN (23911,24483,23757,23915,19722,19726,19720,19724,37605,37604,22852,23913,23917,23919,28352,3514,42125,45647,42243,42124,52907,42242,45645,37607,37606,11567,3764,671,23773,45649,42241,42126)
            AND c.`type`=2 AND c.`status`=5
            ORDER BY c.dateCompleted DESC LIMIT 1000';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/corp/business/open', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT c.contractId, locs.locationName, ch.charName AS issuer, co.corpName AS issuerCorp, a.name AS assignee, c.title, c.dateIssued
            FROM corpcontract AS c
            JOIN `character` AS ch ON c.issuerId=ch.charId
            JOIN corporation AS co ON c.issuerCorpId=co.corpId
            LEFT JOIN dentity AS a ON a.id=c.assigneeId
            LEFT JOIN dlocation AS locs ON c.startLocationId=locs.locationId
            WHERE c.type=2 AND c.status=1 AND c.forCorp=1 AND c.issuerCorpId=1018389948 AND c.dateExpired>NOW()
            ORDER BY c.dateIssued DESC';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/corp/business/finished', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT c.contractId, locs.locationName, ch.charName AS issuer, co.corpName AS issuerCorp, a.name AS assignee, c.title, c.dateCompleted
            FROM corpcontract AS c
            JOIN `character` AS ch ON c.issuerId=ch.charId
            JOIN corporation AS co ON c.issuerCorpId=co.corpId
            LEFT JOIN dentity AS a ON a.id=c.assigneeId
            LEFT JOIN dlocation AS locs ON c.startLocationId=locs.locationId
            WHERE c.type=2 AND c.status=5 AND c.forCorp=1 AND c.issuerCorpId=1018389948
            ORDER BY c.dateCompleted DESC LIMIT 1000';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/topcontractors', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT p.charName AS contractor, sum(c.price) AS total, count(c.price) AS count
            FROM corpcontract AS c
            JOIN `character` As p ON p.charId=c.issuerId
            WHERE c.type=2 AND c.status=1 AND c.forCorp=0 AND c.dateExpired>NOW()
            GROUP BY c.issuerId
            UNION
            SELECT p.corpName AS issuer, sum(c.price) AS total, count(c.price) AS count
            FROM corpcontract AS c
            JOIN corporation AS p ON p.corpId=c.issuerCorpId
            WHERE c.type=2 AND c.status=1 AND c.forCorp=1 AND c.dateExpired>NOW()
            GROUP BY c.issuerCorpId
            ORDER BY total DESC';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/topshippers', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT e.name AS contractor, SUM(c.reward) AS totalreward, SUM(c.volume) AS totalvolume, SUM(c.collateral) AS totalcollat
            FROM corpcontract AS c
            JOIN dentity AS e ON e.id=c.issuerId
            WHERE c.type=4 AND c.status=5 AND c.dateIssued > DATE_ADD(NOW(), INTERVAL -3 MONTH)
            GROUP BY c.issuerId ORDER BY totalvolume DESC LIMIT 100';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/tophaulers', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT e.name AS contractor, SUM(c.reward) AS totalreward, SUM(c.volume) AS totalvolume, SUM(c.collateral) AS totalcollat
            FROM corpcontract AS c
            JOIN dentity AS e ON e.id=c.acceptorId
            WHERE c.type=4 AND c.status=5 AND c.dateIssued > DATE_ADD(NOW(), INTERVAL -3 MONTH)
            GROUP BY c.acceptorId ORDER BY totalvolume DESC LIMIT 100';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/market/open-in-game-contract/{contract}', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->hasActiveChar()) {
        return $response->withJson(array(
            'error' => 'no character linked to this account'
        ));
    }

    // execute the api call
    $header = "Authorization: Bearer " . ($u->getAuthToken());
    $ch = curl_init();
    $url = "https://esi.evetech.net/latest/ui/openwindow/contract/?datasource=tranquility&contract_id=" . $args['contract'];
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_USERAGENT, Dirt\Tools::getProperty('useragent'));
    curl_setopt($ch, CURLOPT_HTTPHEADER, array($header));
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, "");
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 1);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
    $result = curl_exec($ch);
    $httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    $this->logger->debug('/open-in-game-contract sent esi request for user ' . $u->getUserId() . ' type ' . $args['contract']); 
    $this->logger->debug('/open-in-game-contract got response (' . $httpcode . ')');
    $response = $this->cache->denyCache($response);
    return $response->withJson(array(
        'success' => 'made esi call'
    ));
});
