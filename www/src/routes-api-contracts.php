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
    $sql = 'SELECT co.`contractId`, locs.`sName` AS locationId, ich.`name` AS issuerName, co.`price`, co.`title`, co.`dateIssued`
            FROM corpcontract AS co
            LEFT JOIN (
                SELECT charId AS id, charName AS name FROM `character`
                UNION
                SELECT corpId AS id, corpName AS name FROM `corporation`
            ) AS ich ON co.issuerId=ich.id
            LEFT JOIN (
                SELECT `stationId` AS sId,`stationName` AS sName FROM station
                UNION ALL
                SELECT `structId` AS sId,`structName` AS sName FROM structure
            ) AS locs ON co.`startLocationId`=locs.`sId`
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
    $sql = 'SELECT co.`contractId`, locs.`sName` AS locationId, ich.`name` AS issuerName, ach.`name` AS acceptorName, co.`price`, co.`title`, co.`dateAccepted`
        FROM corpcontract AS co
        LEFT JOIN (
            SELECT charId AS id, charName AS name FROM `character`
            UNION
            SELECT corpId AS id, corpName AS name FROM `corporation`
        ) AS ich ON co.issuerId=ich.id
        LEFT JOIN (
            SELECT charId AS id, charName AS name FROM `character`
            UNION
            SELECT corpId AS id, corpName AS name FROM `corporation`
        ) AS ach ON co.acceptorId=ach.id
        LEFT JOIN (
            SELECT `stationId` AS sId,`stationName` AS sName FROM station
            UNION ALL
            SELECT `structId` AS sId,`structName` AS sName FROM structure
        ) AS locs ON co.`startLocationId`=locs.`sId`
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
    $sql = 'SELECT co.`contractId`, ich.`name` AS issuerName, co.`dateIssued`
        FROM corpcontract AS co
        LEFT JOIN (
            SELECT charId AS id, charName AS name FROM `character`
            UNION
            SELECT corpId AS id, corpName AS name FROM `corporation`
        ) AS ich ON co.issuerId=ich.id
        WHERE co.`type`=4 AND co.`status`=1
        ORDER BY co.dateIssued DESC';

    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':userid' => $u->getUserId()
    ));

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/contract/{contractid}/items/', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT i.typeId, t.typeName, i.quantity FROM contractitem AS i
            JOIN invType AS t ON t.typeId=i.typeId WHERE contractId=:contractid';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':contractid' => $args['contractid']
    ));
    if ($stmt->rowCount() > 0) {
        return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
    } else {
        $sql = 'SELECT i.typeId, t.typeName, i.quantity FROM corpcontractitem AS i
            JOIN invType AS t ON t.typeId=i.typeId WHERE contractId=:contractid';
        $stmt = $db->prepare($sql);
        $stmt->execute(array(
            ':contractid' => $args['contractid']
        ));
        return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
    }
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
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        $header
    ));
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
    return $response->withJson(array(
        'success' => 'made esi call'
    ));
});
