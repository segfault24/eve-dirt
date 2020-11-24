<?php

// //////////////////////////////////////////////
// // Imports ////
// //////////////////////////////////////////////

$app->get('/api/jita-buy', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vjitabestbuy';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/jita-buy-xml', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vjitabestbuy';
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

    $sql = 'SELECT typeId, best FROM vjitabestsell';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/jita-sell-xml', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vjitabestsell';
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

    $sql = 'SELECT typeId, best FROM vamarrbestbuy';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/amarr-buy-xml', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vamarrbestbuy';
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

    $sql = 'SELECT typeId, best FROM vamarrbestsell';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/amarr-sell-xml', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vamarrbestsell';
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

    $sql  = 'SELECT `stationId` AS locationId,`stationName` AS locationName FROM station where regionId=:regiona
             UNION ALL
             (
                 SELECT s.`structId` AS locationId, s.`structName` AS locationName FROM structure AS s
                 JOIN dirtstructauth AS a ON s.`structId`=a.`structId`
                 WHERE s.`regionId`=:regionb
             )
             ORDER BY locationName';

    $stmt = $db->prepare("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
    $stmt->execute();
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':regiona' => $args['region'],
        ':regionb' => $args['region']
    ));

    $response = $this->cache->withExpires($response, time() + 1200);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/trade/sell-sell/{source}/{destination}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql  = 'SELECT o.typeId, i.typeName, o.price AS source, o.volumeRemain AS qt, d.best AS dest, i.volume
             FROM marketorder AS o
             JOIN (
               SELECT typeId, MIN(price) AS best FROM marketorder WHERE locationId=:destination AND isBuyOrder=0 GROUP BY typeId, locationId
             ) AS d ON o.typeId=d.typeId
             JOIN invtype AS i ON o.typeId=i.typeId';
    if (intval($args['source']) > 20000000) {
        $sql .= ' WHERE o.locationId=:source';
    } else {
        $sql .= ' WHERE o.regionId=:source';
    }
    $sql .= ' AND o.isBuyOrder=0';
    $sql .= ' AND o.price < d.best';

    $stmt = $db->prepare("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
    $stmt->execute();
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
    $sql .= ' FROM marketorder AS o';
    $sql .= ' JOIN (';
    $sql .= '  SELECT typeId, MAX(price) AS best FROM marketorder WHERE locationId=:destination AND isBuyOrder=1 GROUP BY typeId, locationId';
    $sql .= ' ) AS d ON o.typeId=d.typeId';
    $sql .= ' JOIN invtype AS i ON o.typeId=i.typeId';
    if (intval($args['source']) > 20000000) {
        $sql .= ' WHERE o.locationId=:source';
    } else {
        $sql .= ' WHERE o.regionId=:source';
    }
    $sql .= ' AND o.isBuyOrder=0';
    $sql .= ' AND o.price < d.best';

    $stmt = $db->prepare("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
    $stmt->execute();
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':source' => $args['source'],
        ':destination' => $args['destination']
    ));

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/trade/import/{source}/{destination}', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    // get destination region if destination is a struct/station
    $destregionid = intval($args['destination']);
    if ($destregionid > 20000000) {
        $sql  = 'SELECT `regionId` FROM station WHERE stationId=:locationa
                 UNION ALL
                 SELECT `regionId` FROM structure WHERE structId=:locationb';
        $stmt = $db->prepare("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
        $stmt->execute();
        $stmt = $db->prepare($sql);
        $stmt->execute(array(
            ':locationa' => $args['destination'],
            ':locationb' => $args['destination']
        ));
        // 404 not found if the location is unknown
        if ($stmt->rowCount() < 1) {
            return $response->withStatus(404);
        }
        $regioninfo = $stmt->fetch(PDO::FETCH_ASSOC);
        $destregionid = $regioninfo['regionId'];
    }

    $sql  = 'SELECT inv.typeId, inv.typeName, inv.volume, src.source, dst.dest, dst.stock, stat.ma30, stat.ma90 FROM marketstat stat';
    $sql .= ' JOIN invtype inv ON inv.typeId=stat.typeId';
    $sql .= ' JOIN (';
    $sql .= '  SELECT typeId, MIN(price) AS source FROM marketorder';
    if (intval($args['source']) > 20000000) {
        $sql .= '  WHERE locationId=:source';
    } else {
        $sql .= '  WHERE regionId=:source';
    }
    $sql .= '   AND isBuyOrder=0 GROUP BY typeId';
    $sql .= ' ) src ON src.typeId=stat.typeId';
    $sql .= ' JOIN (';
    $sql .= '  SELECT typeId, MIN(price) AS dest, SUM(volumeRemain) AS stock FROM marketorder';
    if (intval($args['destination']) > 20000000) {
        $sql .= '  WHERE locationId=:destination';
    } else {
        $sql .= '  WHERE regionId=:destination';
    }
    $sql .= '   AND isBuyOrder=0 GROUP BY typeId';
    $sql .= ' ) dst ON dst.typeId=stat.typeId';
    $sql .= ' WHERE src.source < dst.dest';
    $sql .= ' AND stat.regionId=:destregionid';
    $sql .= ' AND stat.ma30 > 0';

    $stmt = $db->prepare("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
    $stmt->execute();
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':source' => $args['source'],
        ':destination' => $args['destination'],
        ':destregionid' => $destregionid
    ));

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

