<?php
use Dirt\Tools;

// //////////////////////////////////////////////
// // Contract Pages ////
// //////////////////////////////////////////////

$app->get('/contracts', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'contracts.phtml', $args);
});

$app->get('/contract/{contractid}', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    $db = Dirt\Database::getDb();
    // try the personal contract table for exchange
    $sql = 'SELECT ie.name AS issuer, sl.locationName, ae.name AS assignee, c.price, cs.value AS status, ca.value AS availability, c.title, c.dateIssued, c.dateCompleted
            FROM contract AS c
            JOIN contractstatus AS cs ON cs.id=c.status
            JOIN contractavailability AS ca ON ca.id=c.availability
            LEFT JOIN dlocation AS sl ON sl.locationId=c.startLocationId
            LEFT JOIN dentity AS ie ON ie.id=c.issuerId
            LEFT JOIN dentity AS ae ON ae.id=c.assigneeId
            WHERE c.contractId=:contractid';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(':contractid' => $args['contractid']));
    if ($stmt->rowCount() > 0) {
        // pass info to the template
        $args['cinfo'] = $stmt->fetchAll(PDO::FETCH_ASSOC)[0];
        // get the items
        $sql = 'SELECT i.typeId, t.typeName, i.quantity FROM contractitem AS i
                JOIN invtype AS t ON t.typeId=i.typeId
                WHERE contractId=:contractid AND i.included=1';
        $stmt = $db->prepare($sql);
        $stmt->execute(array(':contractid' => $args['contractid']));
        if ($stmt->rowCount() > 0) {
            $args['offeritems'] = $stmt->fetchAll(PDO::FETCH_ASSOC);
        }
        $sql = 'SELECT i.typeId, t.typeName, i.quantity FROM contractitem AS i
                JOIN invtype AS t ON t.typeId=i.typeId
                WHERE contractId=:contractid AND i.included=0';
        $stmt = $db->prepare($sql);
        $stmt->execute(array(':contractid' => $args['contractid']));
        if ($stmt->rowCount() > 0) {
            $args['askitems'] = $stmt->fetchAll(PDO::FETCH_ASSOC);
        }
        $response = $this->cache->withExpires($response, time() + 300);
        return $this->renderer->render($response, 'contract.phtml', $args);
    }

    // else try the corp contract table for exchange
    $sql = 'SELECT ie.name AS issuer, sl.locationName, ae.name AS assignee, c.price, cs.value AS status, ca.value AS availability, c.title, c.dateIssued, c.dateCompleted
            FROM corpcontract AS c
            JOIN contractstatus AS cs ON cs.id=c.status
            JOIN contractavailability AS ca ON ca.id=c.availability
            LEFT JOIN dlocation AS sl ON sl.locationId=c.startLocationId
            LEFT JOIN dentity AS ie ON ie.id=c.issuerId
            LEFT JOIN dentity AS ae ON ae.id=c.assigneeId
            WHERE c.contractId=:contractid';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(':contractid' => $args['contractid']));
    if ($stmt->rowCount() > 0) {
        // pass info to the template
        $args['cinfo'] = $stmt->fetchAll(PDO::FETCH_ASSOC)[0];
        // get the items
        $sql = 'SELECT i.typeId, t.typeName, i.quantity FROM corpcontractitem AS i
                JOIN invtype AS t ON t.typeId=i.typeId
                WHERE contractId=:contractid AND i.included=1';
        $stmt = $db->prepare($sql);
        $stmt->execute(array(':contractid' => $args['contractid']));
        if ($stmt->rowCount() > 0) {
            $args['offeritems'] = $stmt->fetchAll(PDO::FETCH_ASSOC);
        }
        $sql = 'SELECT i.typeId, t.typeName, i.quantity FROM corpcontractitem AS i
                JOIN invtype AS t ON t.typeId=i.typeId
                WHERE contractId=:contractid AND i.included=0';
        $stmt = $db->prepare($sql);
        $stmt->execute(array(':contractid' => $args['contractid']));
        if ($stmt->rowCount() > 0) {
            $args['askitems'] = $stmt->fetchAll(PDO::FETCH_ASSOC);
        }
        $response = $this->cache->withExpires($response, time() + 300);
        return $this->renderer->render($response, 'contract.phtml', $args);
    }

    // else fail
    return $this->renderer->render($response, 'contract.phtml', $args);
});

$app->get('/doctrines', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
        ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    $db = Dirt\Database::getDb();
    $sql = 'SELECT l.listId, l.name, s.structName, d.quantity, d.target, d.lowestPrice
            FROM doctrine AS d
            JOIN dirtlist AS l ON d.listId=l.listId
            JOIN structure AS s ON d.locationId=s.structId';
    $stmt = $db->prepare($sql);
    $stmt->execute();

    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $args['doclist'] = $rows;

    $response = $this->cache->withExpires($response, time() + 300);
    return $this->renderer->render($response, 'doctrines.phtml', $args);
});
