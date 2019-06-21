<?php

// //////////////////////////////////////////////
// // Imports ////
// //////////////////////////////////////////////

$app->get('/api/jita-buy', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vJitaBestBuy';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/jita-buy-xml', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vJitaBestBuy';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    echo '<?xml version="1.0" encoding="UTF-8"?>' . "\r\n";
    echo '<types>' . "\r\n";
    while ($row = $stmt->fetch(PDO::FETCH_NUM, PDO::FETCH_ORI_NEXT)) {
        $xml = '<type>';
        $xml .= '<typeId>' . $row[0] . '</typeId>';
        $xml .= '<bestBuy>' . $row[1] . '</bestBuy>';
        $xml .= '</type>' . "\r\n";
        echo $xml;
    }
    echo '</types>' . "\r\n";

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withHeader('Content-Type', 'text/xml');
});

$app->get('/api/jita-sell', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vJitaBestSell';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/jita-sell-xml', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vJitaBestSell';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    echo '<?xml version="1.0" encoding="UTF-8"?>' . "\r\n";
    echo '<types>' . "\r\n";
    while ($row = $stmt->fetch(PDO::FETCH_NUM, PDO::FETCH_ORI_NEXT)) {
        $xml = '<type>';
        $xml .= '<typeId>' . $row[0] . '</typeId>';
        $xml .= '<bestSell>' . $row[1] . '</bestSell>';
        $xml .= '</type>' . "\r\n";
        echo $xml;
    }
    echo '</types>' . "\r\n";

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withHeader('Content-Type', 'text/xml');
});

$app->get('/api/amarr-buy', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vAmarrBestBuy';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/amarr-buy-xml', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vAmarrBestBuy';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    echo '<?xml version="1.0" encoding="UTF-8"?>' . "\r\n";
    echo '<types>' . "\r\n";
    while ($row = $stmt->fetch(PDO::FETCH_NUM, PDO::FETCH_ORI_NEXT)) {
        $xml = '<type>';
        $xml .= '<typeId>' . $row[0] . '</typeId>';
        $xml .= '<bestBuy>' . $row[1] . '</bestBuy>';
        $xml .= '</type>' . "\r\n";
        echo $xml;
    }
    echo '</types>' . "\r\n";

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withHeader('Content-Type', 'text/xml');
});

$app->get('/api/amarr-sell', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vAmarrBestSell';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/amarr-sell-xml', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vAmarrBestSell';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    echo '<?xml version="1.0" encoding="UTF-8"?>' . "\r\n";
    echo '<types>' . "\r\n";
    while ($row = $stmt->fetch(PDO::FETCH_NUM, PDO::FETCH_ORI_NEXT)) {
        $xml = '<type>';
        $xml .= '<typeId>' . $row[0] . '</typeId>';
        $xml .= '<bestSell>' . $row[1] . '</bestSell>';
        $xml .= '</type>' . "\r\n";
        echo $xml;
    }
    echo '</types>' . "\r\n";

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withHeader('Content-Type', 'text/xml');
});


// //////////////////////////////////////////////
// // Exports ////
// //////////////////////////////////////////////

$app->get('/api/trade/structs-by-region/{region}/', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql  = 'SELECT `stationId` AS sId,`stationName` AS sName FROM station where regionId=:regiona';
    $sql .= ' UNION ALL';
    $sql .= ' SELECT `structId` AS sId,`structName` AS sName FROM structure where regionId=:regionb';
    $sql .= ' ORDER BY sName';

    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':regiona' => $args['region'],
        ':regionb' => $args['region']
    ));

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/trade/sell-sell/{source}/{destination}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql  = 'SELECT o.typeId, i.typeName, o.price AS source, o.volumeRemain AS qt, d.best AS dest, i.volume
             FROM marketOrder AS o
             JOIN (
               SELECT typeId, MIN(price) AS best FROM marketOrder WHERE locationId=:destination AND isBuyOrder=0 GROUP BY typeId, locationId
             ) AS d ON o.typeId=d.typeId
             JOIN invType AS i ON o.typeId=i.typeId';
    if (intval($args['source']) > 20000000) {
        $sql .= ' WHERE o.locationId=:source';
    } else {
        $sql .= ' WHERE o.regionId=:source';
    }
    $sql .= ' AND o.isBuyOrder=0';
    $sql .= ' AND o.price < d.best';

    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':source' => $args['source'],
        ':destination' => $args['destination']
    ));

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/trade/sell-buy/{source}/{destination}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql  = 'SELECT o.typeId, i.typeName, o.price AS source, o.volumeRemain AS qt, d.best AS dest, i.volume';
    $sql .= ' FROM marketOrder AS o';
    $sql .= ' JOIN (';
    $sql .= '  SELECT typeId, MAX(price) AS best FROM marketOrder WHERE locationId=:destination AND isBuyOrder=1 GROUP BY typeId, locationId';
    $sql .= ' ) AS d ON o.typeId=d.typeId';
    $sql .= ' JOIN invType AS i ON o.typeId=i.typeId';
    if (intval($args['source']) > 20000000) {
        $sql .= ' WHERE o.locationId=:source';
    } else {
        $sql .= ' WHERE o.regionId=:source';
    }
    $sql .= ' AND o.isBuyOrder=0';
    $sql .= ' AND o.price < d.best';

    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':source' => $args['source'],
        ':destination' => $args['destination']
    ));

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});
