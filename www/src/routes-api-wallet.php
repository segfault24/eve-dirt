<?php

// //////////////////////////////////////////////
// // Wallet Data ////
// //////////////////////////////////////////////

$app->get('/api/wallet/orders', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT
            a.`charName`, o.`orderId`, o.`typeId`, i.`typeName`, r.`regionName`, locs.`sName`, o.`isBuyOrder`, o.`price`,
            o.`range`, o.`duration`, o.`volumeRemain`, o.`volumeTotal`, o.`minVolume`, o.`issued`
            FROM charOrder AS o
            LEFT JOIN invType AS i ON o.`typeId`=i.`typeId`
            LEFT JOIN region AS r ON o.`regionId`=r.`regionId`
            LEFT JOIN dirtApiAuth AS a ON o.`charId`=a.`charId`
            LEFT JOIN (
                SELECT `stationId` AS sId,`stationName` AS sName FROM station
                UNION ALL
                SELECT `structId` AS sId,`structName` AS sName FROM structure
            ) locs ON o.`locationId`=locs.`sId`
            WHERE o.`charId` IN (
                SELECT charId FROM dirtApiAuth WHERE userId=:userid
            )';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':userid' => $u->getUserId()
    ));

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/wallet/orderids', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT `orderId` FROM charOrder WHERE `charId` IN (
                SELECT charId FROM dirtApiAuth WHERE userId=:userid
            )';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':userid' => $u->getUserId()
    ));

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/wallet/transactions', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT t.`date`,a.`charName`,i.`typeId`,i.`typeName`,t.`isBuy`,t.`quantity`,t.`unitPrice` FROM walletTransaction AS t
            JOIN invType AS i ON i.typeId=t.typeId
            JOIN dirtApiAuth AS a ON a.charId=t.charId
            WHERE t.charId IN (
                SELECT charId FROM dirtApiAuth WHERE userId=:userid
            ) ORDER BY DATE DESC LIMIT 1000;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':userid' => $u->getUserId()
    ));

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/wallet/journal', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT j.`date`,a.`charName`,j.`refType`,j.`amount`,j.`balance`,j.`description` FROM walletJournal AS j
            JOIN dirtApiAuth AS a ON a.charId=j.charId
            WHERE j.charId IN (
                SELECT charId FROM dirtApiAuth WHERE userId=:userid
            ) ORDER BY DATE DESC LIMIT 1000;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':userid' => $u->getUserId()
    ));

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/wallet/contracts', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT co.`contractId`, ich.`name` AS issuerName, co.`type`, co.`status`, co.`dateIssued`, co.`dateCompleted`, ach.`name` AS acceptorName
            FROM contract AS co
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
            WHERE co.`issuerId` in (
            SELECT charId FROM dirtApiAuth WHERE userId=:userid
            )
            ORDER BY co.dateIssued DESC';

    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':userid' => $u->getUserId()
    ));

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/wallet/returns', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    $db = Dirt\Database::getDb();
    $sql = 'SELECT s.date, i.typeId, i.typeName, b.buy, s.sell
            FROM (
              SELECT t.typeId, t.unitPrice AS buy
              FROM walletTransaction AS t
              JOIN (
                SELECT typeId, MAX(date) as maxDate
                FROM walletTransaction
                WHERE isBuy=1
                AND charId IN (SELECT charId FROM dirtApiAuth WHERE userId=:userida)
                GROUP BY typeId
              ) AS lbuy ON t.typeId=lbuy.typeId AND t.date=lbuy.maxDate
              ORDER BY t.date DESC LIMIT 100
            ) AS b
            INNER JOIN (
              SELECT t.date, t.typeId, t.unitPrice AS sell
              FROM walletTransaction AS t
              JOIN (
                SELECT typeId, MAX(date) as maxDate
                FROM walletTransaction
                WHERE isBuy=0
                AND charId IN (SELECT charId FROM dirtApiAuth WHERE userId=:useridb)
                GROUP BY typeId
              ) AS lsell ON t.typeId=lsell.typeId AND t.date=lsell.maxDate
              ORDER BY t.date DESC LIMIT 100
            ) AS s ON b.typeId=s.typeId
            JOIN invType AS i ON i.typeId=b.typeId
           ';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':userida' => $u->getUserId(),
        ':useridb' => $u->getUserId()
    ));

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});



