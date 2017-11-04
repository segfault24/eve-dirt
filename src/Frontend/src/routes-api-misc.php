<?php

////////////////////////////////////////////////
////             Static Data                ////
////////////////////////////////////////////////

/*$app->get('/api/regions', function ($request, $response, $args) {
	$db = Dirt\Database::getDb();

	$sql = 'SELECT `regionID`, `regionName` FROM evesde.mapRegions;';
	$stmt = $db->prepare($sql);
	$stmt->execute();

	return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});*/

/*$app->get('/api/stations', function ($request, $response, $args) {
	$db = Dirt\Database::getDb();

	$sql = 'SELECT `stationID`, `stationName` FROM evesde.staStations;';
	$stmt = $db->prepare($sql);
	$stmt->execute();

	return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});*/

$app->get('/api/search-types', function ($request, $response, $args) {
	$db = Dirt\Database::getDb();

	$sql = 'SELECT typeID AS value, typeName AS label FROM invTypes WHERE published=1 ORDER BY typeName;';
	$stmt = $db->prepare($sql);
	$stmt->execute();

	return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/market-groups', function ($request, $response, $args) {
	$db = Dirt\Database::getDb();

	$sql = 'SELECT marketGroupID, marketGroupName, parentGroupID, hasTypes FROM invMarketGroups ORDER BY marketGroupName;';
	$stmt = $db->prepare($sql);
	$stmt->execute();

	return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/market-types', function ($request, $response, $args) {
	$db = Dirt\Database::getDb();

	$sql = 'SELECT typeID, typeName, marketGroupID FROM invTypes WHERE published=1 AND marketGroupID IS NOT NULL ORDER BY typeName;';
	$stmt = $db->prepare($sql);
	$stmt->execute();

	return $response->withJson($stmt->fetchAll(PDO::FETCH_ASSOC));
});

$app->get('/api/types/{typeid}', function ($request, $response, $args) {
	$db = Dirt\Database::getDb();

	$sql = 'SELECT typeID, typeName, volume, marketGroupID FROM invTypes WHERE typeID=:typeid;';
	$stmt = $db->prepare($sql);
	//$stmt->bindParam(':typeid', $args['typeid']);
	$stmt->execute(array(':typeid'=>$args['typeid']));

	return $response->withJson($stmt->fetch(PDO::FETCH_ASSOC));
});

$app->get('/api/market-group/{group}', function ($request, $response, $args) {
	$db = Dirt\Database::getDb();

	$sql = 'SELECT marketGroupID, marketGroupName FROM invMarketGroups WHERE marketGroupID=:group;';
	$stmt = $db->prepare($sql);
	$stmt->bindParam(':group', $args['group']);
	$stmt->execute();

	$output['groups'] = $stmt->fetchAll(PDO::FETCH_ASSOC);

	$sql = 'SELECT typeID, typeName FROM invTypes WHERE marketGroupID=:group;';
	$stmt = $db->prepare($sql);
	$stmt->bindParam(':group', $args['group']);
	$stmt->execute();

	$output['types'] = $stmt->fetchAll(PDO::FETCH_ASSOC);

	return $response->withJson($output);
});

