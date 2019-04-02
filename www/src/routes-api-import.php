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

$app->get('/api/staging-sell', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vStagingBestSell';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/staging-sell-xml', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vStagingBestSell';
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

$app->get('/api/home-sell', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vHomeBestSell';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/home-sell-xml', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql = 'SELECT typeId, best FROM vHomeBestSell';
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

$app->get('/api/staging-sell-to-jita-sell', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql  = 'SELECT s.typeId, i.typeName, s.best AS source, d.best AS dest, i.volume';
    $sql .= ' FROM vJitaBestSell AS d';
    $sql .= ' JOIN vStagingBestSell AS s ON d.typeId=s.typeId';
    $sql .= ' JOIN invTypes AS i ON i.typeID=s.typeId';
    $sql .= ' WHERE s.best < d.best';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/staging-sell-to-jita-buy', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql  = 'SELECT s.typeId, i.typeName, s.best AS source, d.best AS dest, i.volume';
    $sql .= ' FROM vJitaBestBuy AS d';
    $sql .= ' JOIN vStagingBestSell AS s ON d.typeId=s.typeId';
    $sql .= ' JOIN invTypes AS i ON i.typeID=s.typeId';
    $sql .= ' WHERE s.best < d.best';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/home-sell-to-jita-sell', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql  = 'SELECT s.typeId, i.typeName, s.best AS source, d.best AS dest, i.volume';
    $sql .= ' FROM vJitaBestSell AS d';
    $sql .= ' JOIN vHomeBestSell AS s ON d.typeId=s.typeId';
    $sql .= ' JOIN invTypes AS i ON i.typeID=s.typeId';
    $sql .= ' WHERE s.best < d.best';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/home-sell-to-jita-buy', function ($request, $response, $args) {
    $db = Dirt\Database::getDb();

    $sql  = 'SELECT s.typeId, i.typeName, s.best AS source, d.best AS dest, i.volume';
    $sql .= ' FROM vJitaBestBuy AS d';
    $sql .= ' JOIN vHomeBestSell AS s ON d.typeId=s.typeId';
    $sql .= ' JOIN invTypes AS i ON i.typeID=s.typeId';
    $sql .= ' WHERE s.best < d.best';

    $stmt = $db->prepare($sql);
    $stmt->execute();

    $response = $this->cache->withExpires($response, time() + 300);
    return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});
