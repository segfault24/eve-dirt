<?php

// //////////////////////////////////////////////
// // Market Data ////
// //////////////////////////////////////////////
$app->get('/api/market/history/{region}/type/{type}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT `typeId`, `regionId`, `date`, `highest`,
            `average`, `lowest`, `volume`, `orderCount`
            FROM marketHistory
            WHERE `regionId`=:region
            AND `typeId`=:type
            ORDER BY `date` ASC;';
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':region', $args['region']);
    $stmt->bindParam(':type', $args['type']);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/market/orders/{location}/type/{type}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT
            o.`orderId`, o.`typeId`, r.`regionName`, locs.`sName`, o.`isBuyOrder`, o.`price`,
            o.`range`, o.`duration`, o.`volumeRemain`, o.`volumeTotal`, o.`minVolume`, o.`issued`
            FROM marketOrder AS o
            LEFT JOIN mapRegions AS r ON o.`regionId`=r.`regionID`
            LEFT JOIN (
                SELECT `stationID` AS sId,`stationName` AS sName FROM staStations 
                UNION ALL
                SELECT `structId` AS sId,`structName` AS sName FROM structure
            ) locs ON o.`locationId`=locs.`sId`
            WHERE';
    if ($args['location'] != 0) {
        $sql .= ' (o.`regionId`=:location OR o.`locationId`=:location) AND';
    }
    $sql .= ' o.`typeId`=:type;';
    $stmt = $db->prepare($sql);
    if ($args['location'] != 0) {
        $stmt->bindParam(':location', $args['location']);
    }
    $stmt->bindParam(':type', $args['type']);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

/*
 * $app->post('/api/market/prices/{location}[/{buysell}]', function ($request, $response, $args) {
 * // SELECT o.type_id, MIN(o.price) AS sell FROM evemrkt.marketorders AS o
 * // WHERE o.is_buy_order=0 AND o.type_id IN (SELECT typeid FROM listitems WHERE listid=48)
 * // AND (o.region_id=10000002 OR o.location_id=60003760)
 * // GROUP BY o.type_id
 * return $response->withJson();
 * });
 */

/*
 * $app->post('/api/market/volume/{location}[/length/{length}]', function ($request, $response, $args) {
 * // SELECT `type_id`, `region_id`, AVG(`volume`) as avgvol
 * // FROM evemrkt.marketHistory
 * // WHERE DATEDIFF(`date`, CURDATE())<90
 * // AND `region_id`=?
 * // AND `type_id` IN ('.str_repeat('?,', count($types)-1).'?)
 * // GROUP BY `type_id`;
 * return $response->withJson();
 * });
 */

$app->get('/api/market/open-in-game/{type}', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->hasActiveChar()) {
        return $response->withJson(array(
            'error' => 'no character linked to this account'
        ));
    }

    // execute the api call
    $header = "Authorization: Bearer " . ($u->getAuthToken());
    $ch = curl_init();
    $url = "https://esi.evetech.net/latest/ui/openwindow/marketdetails/?datasource=tranquility&type_id=" . $args['type'];
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
    curl_close($ch);

    $this->logger->debug('/open-in-game sent esi request for character ' . $u->getUserId() . ' type ' . $args['type']); 
    return $response->withJson(array(
        'success' => 'made esi call'
    ));
});

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

$app->get('/api/insurance-prices', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT t.typeID, t.typeName, i.name, i.cost, i.payout' . ' FROM insurancePrice AS i' . ' JOIN invTypes AS t ON i.typeId=t.typeID' . ' WHERE t.marketGroupID IS NOT NULL' . ' AND published=1';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':typeid' => $args['typeid']
    ));

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/insurance-price/{typeid}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT name, cost, payout FROM insurancePrice WHERE typeId=:typeid';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':typeid' => $args['typeid']
    ));

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});
