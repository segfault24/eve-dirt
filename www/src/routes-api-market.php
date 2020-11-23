<?php

// //////////////////////////////////////////////
// // Market Data ////
// //////////////////////////////////////////////

$app->get('/api/market/history/{region}/type/{type}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT `typeId`, `regionId`, `date`, `highest`,
            `average`, `lowest`, `volume`, `orderCount`
            FROM markethistory
            WHERE `regionId`=:region
            AND `typeId`=:type
            ORDER BY `date` ASC;';
    $stmt = $db->prepare("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
    $stmt->execute();
    $stmt = $db->prepare($sql);
    $stmt->bindParam(':region', $args['region']);
    $stmt->bindParam(':type', $args['type']);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/market/orders/{location}/type/{type}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT
            o.`orderId`, o.`typeId`, r.`regionName`, locs.`locationName`, o.`isBuyOrder`, o.`price`,
            o.`range`, o.`duration`, o.`volumeRemain`, o.`volumeTotal`, o.`minVolume`, o.`issued`
            FROM marketorder AS o
            LEFT JOIN region AS r ON o.`regionId`=r.`regionId`
            LEFT JOIN dlocation AS locs ON o.`locationId`=locs.`locationId`
            WHERE o.`typeId`=:type';
    if ($args['location'] != 0) {
        $sql .= ' AND (o.`regionId`=:location OR o.`locationId`=:location);';
    }
    $stmt = $db->prepare("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
    $stmt->execute();
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
 * // FROM markethistory
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
    $httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    $this->logger->debug('/open-in-game sent esi request for character ' . $u->getUserId() . ' type ' . $args['type']); 
    $this->logger->debug('/open-in-game got response (' . $httpcode . ')');
    return $response->withJson(array(
        'success' => 'made esi call'
    ));
});

$app->get('/api/insurance-prices', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT t.typeId, t.typeName, i.name, i.cost, i.payout' . ' FROM insuranceprice AS i' . ' JOIN invtype AS t ON i.typeId=t.typeId' . ' WHERE t.marketGroupId IS NOT NULL' . ' AND published=1';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/insurance-price/{typeid}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT name, cost, payout FROM insuranceprice WHERE typeId=:typeid';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':typeid' => $args['typeid']
    ));

    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

