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
            FROM marketOrder AS o
            LEFT JOIN invTypes AS i ON o.`typeId`=i.`typeID`
            LEFT JOIN mapRegions AS r ON o.`regionId`=r.`regionID`
            LEFT JOIN dirtApiAuth AS a ON o.`charId`=a.`charId`
            LEFT JOIN (
                SELECT `stationID` AS sId,`stationName` AS sName FROM staStations 
                UNION ALL
                SELECT `structId` AS sId,`structName` AS sName FROM structure
            ) locs ON o.`locationId`=locs.`sId`
            WHERE o.`source`=2 AND o.`charId` IN (
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
    $sql = 'SELECT t.`date`,a.`charName`,i.`typeID`,i.`typeName`,t.`isBuy`,t.`quantity`,t.`unitPrice` FROM walletTransaction AS t
            JOIN invTypes AS i ON i.typeID=t.typeId
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

