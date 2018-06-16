<?php

// //////////////////////////////////////////////
// // List Management ////
// //////////////////////////////////////////////

// lists collection
$app->get('/api/lists/', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    // retrieve all the user's lists
    $db = Dirt\Database::getDb();
    $sql = 'SELECT listId, userId, name, public FROM dirtList WHERE userId=:userid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':userid' => $u->getUserId()
    ));

    $lists = $stmt->fetchAll(PDO::FETCH_ASSOC);

    return $response->withJson($lists);
});

$app->post('/api/lists/', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    // set default parameters if necessary
    $listname = $request->getParsedBody()['info']['name'];
    if ($listname == '') {
        $listname = 'list ' . uniqid();
    }
    $public = $request->getParsedBody()['public'];
    if ($public == '') {
        $public = false;
    }

    // create the new list
    $db = Dirt\Database::getDb();
    $sql = 'INSERT INTO dirtList (name, userId, public) VALUES (:listname, :userid, FALSE);';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listname' => $listname,
        ':userid' => $u->getUserId()
    ));

    return $response;
});

// list resource

$app->get('/api/lists/{listid}', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    // retrieve the list's info
    $db = Dirt\Database::getDb();
    $sql = 'SELECT listId, userId, name, public FROM dirtList WHERE listId=:listid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid']
    ));

    // 404 not found if the list doesn't exist
    if ($stmt->rowCount() == 0) {
        return $response->withStatus(404);
    }

    $listinfo = $stmt->fetch(PDO::FETCH_ASSOC);

    // 403 forbidden if the user doesn't own the list and it's not public
    if ($listinfo['public'] != 1 && $listinfo['userId'] != $u->getUserId()) {
        return $response->withStatus(403);
    }

    return $response->withJson($listinfo);
});

$app->put('/api/lists/{listid}', function ($request, $response, $args) { // TODO
    return $response;
});

$app->delete('/api/lists/{listid}', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    // does the list exist and is it their list?
    $db = Dirt\Database::getDb();
    $sql = 'SELECT listId, userId FROM dirtList WHERE listId=:listid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid']
    ));

    // 404 not found if the list doesn't exist
    if ($stmt->rowCount() == 0) {
        return $response->withStatus(404);
    }

    $listinfo = $stmt->fetch(PDO::FETCH_ASSOC);

    // 403 forbidden if the user doesn't own the list
    if ($listinfo['userId'] != $u->getUserId()) {
        return $response->withStatus(403);
    }

    // remove listitems
    $sql = 'DELETE FROM dirtListItem WHERE listId=:listid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid']
    ));

    // remove list
    $sql = 'DELETE FROM dirtList WHERE listId=:listid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid']
    ));

    return $response;
});

// list items collection

$app->get('/api/lists/{listid}/types/', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    // retrieve the list's info
    $db = Dirt\Database::getDb();
    $sql = 'SELECT listId, userId, public FROM dirtList WHERE listId=:listid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid']
    ));

    // 404 not found if the list doesn't exist
    if ($stmt->rowCount() == 0) {
        return $response->withStatus(404);
    }

    $listinfo = $stmt->fetch(PDO::FETCH_ASSOC);

    // 403 forbidden if the user doesn't own the list and it's not public
    if ($listinfo['public'] != 1 && $listinfo['userId'] != $u->getUserId()) {
        return $response->withStatus(403);
    }

    // retrieve the items in the list
    $sql = 'SELECT li.typeId, i.typeName, li.quantity FROM dirtListItem AS li JOIN invTypes AS i ON li.typeId=i.typeID WHERE li.listId=:listid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid']
    ));

    $listitems = $stmt->fetchAll(PDO::FETCH_ASSOC);

    return $response->withJson($listitems);
});

$app->put('/api/lists/{listid}/types/', function ($request, $response, $args) { // TODO
    return $response;
});

$app->delete('/api/lists/{listid}/types/', function ($request, $response, $args) { // TODO
    return $response;
});

// list item resource

$app->get('/api/lists/{listid}/types/{typeid}', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    // retrieve the list's info
    $db = Dirt\Database::getDb();
    $sql = 'SELECT listId, userId, public FROM dirtList WHERE listId=:listid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid']
    ));

    // 404 not found if the list doesn't exist
    if ($stmt->rowCount() == 0) {
        return $response->withStatus(404);
    }

    $listinfo = $stmt->fetch(PDO::FETCH_ASSOC);

    // 403 forbidden if the user doesn't own the list and it's not public
    if ($listinfo['public'] != 1 && $listinfo['userId'] != $u->getUserId()) {
        return $response->withStatus(403);
    }

    // retrieve the item from the list
    $sql = 'SELECT li.typeId, i.typeName, li.quantity FROM dirtListItem AS li JOIN invTypes AS i ON li.typeId=i.typeID WHERE li.listId=:listid AND li.typeId=:typeid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid'],
        ':typeid' => $args['typeid']
    ));

    // 404 not found if the item isn't in the list
    if ($stmt->rowCount() == 0) {
        return $response->withStatus(404);
    }

    $listitems = $stmt->fetch(PDO::FETCH_ASSOC);

    return $response->withJson($listitems);
});

$app->put('/api/lists/{listid}/types/{typeid}', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(401);
    }

    // retrieve the list's info
    $db = Dirt\Database::getDb();
    $sql = 'SELECT listId, userId, public FROM dirtList WHERE listId=:listid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid']
    ));

    // 404 not found if the list doesn't exist
    if ($stmt->rowCount() == 0) {
        return $response->withStatus(404);
    }

    $listinfo = $stmt->fetch(PDO::FETCH_ASSOC);

    // 403 forbidden if the user doesn't own the list
    if ($listinfo['userId'] != $u->getUserId()) {
        return $response->withStatus(403);
    }

    // check for exact match first
    $typeName = $request->getParsedBody()['typeName'];
    $sql = 'SELECT typeID FROM invTypes WHERE typeName=:typeName AND published=1;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':typeName' => $typeName
    ));

    if ($stmt->rowCount() == 1) {
        // exact match found, do nothing & fall through
    } else if ($stmt->rowCount() > 1) {
        // 400 bad request
        // multiple exact matches somehow???
        return $response->withStatus(400);
    } else {
        // no exact matchs
        // check if there's something close enough that is unique
        $typeName = '%' . $typeName . '%';
        $sql = 'SELECT typeID FROM invTypes WHERE typeName LIKE :typeName AND published=1;';
        $stmt = $db->prepare($sql);
        $stmt->execute(array(
            ':typeName' => $typeName
        ));

        if ($stmt->rowCount() == 1) {
            // found something that is close enough, do nothing & fall through
        } else {
            // 400 bad request
            // no exact match or anything close enough that is unique
            return $response->withStatus(400);
        }
    }

    $typeid = $stmt->fetch(PDO::FETCH_ASSOC)['typeID'];

    // get the quantity
    $quantity = $request->getParsedBody()['quantity'];
    if ($quantity == '') {
        // check if the type is already in the list
        // we don't want to overwrite the qt if the PUT didn't specify a quantity
        $sql = 'SELECT typeId FROM dirtList WHERE listId=:listid AND typeId=:typeid;';
        $stmt = $db->prepare($sql);
        $stmt->execute(array(
            ':listid' => $args['listid'],
            ':typeid' => $typeid
        ));
        if ($stmt->rowCount() == 1) {
            return response;
        }

        // set the default qt
        $quantity = 1;
    }
    $quantity = intval($quantity); // ensure integer

    // replace into the list
    $sql = 'REPLACE INTO dirtListItem (listId, typeId, quantity) VALUES (:listid, :typeid, :quantity);';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid'],
        ':typeid' => $typeid,
        ':quantity' => $quantity
    ));

    return $response;
});

$app->delete('/api/lists/{listid}/types/{typeid}', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }

    // retrieve the list's info
    $db = Dirt\Database::getDb();
    $sql = 'SELECT listId, userId, public FROM dirtList WHERE listId=:listid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid']
    ));

    // 404 not found if the list doesn't exist
    if ($stmt->rowCount() == 0) {
        return $response->withStatus(404);
    }

    $listinfo = $stmt->fetch(PDO::FETCH_ASSOC);

    // 403 forbidden if the user doesn't own the list
    if ($listinfo['userId'] != $u->getUserId()) {
        return $response->withStatus(403);
    }

    // retrieve the item from the list
    $sql = 'SELECT typeId FROM dirtListItem WHERE listId=:listid AND typeId=:typeid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid'],
        ':typeid' => $args['typeid']
    ));

    // 404 not found if the item isn't in the list
    if ($stmt->rowCount() == 0) {
        return $response->withStatus(404);
    }

    // remove the listitem
    $sql = 'DELETE FROM dirtListItem WHERE listId=:listid AND typeId=:typeid;';
    $stmt = $db->prepare($sql);
    $stmt->execute(array(
        ':listid' => $args['listid'],
        ':typeid' => $args['typeid']
    ));

    return $response;
});

