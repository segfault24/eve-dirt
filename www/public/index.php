<?php
if (PHP_SAPI == 'cli-server') {
    // To help the built-in PHP dev server, check if the request was actually for
    // something which should probably be served as a static file
    $url  = parse_url($_SERVER['REQUEST_URI']);
    $file = __DIR__ . $url['path'];
    if (is_file($file)) {
        return false;
    }
}

require __DIR__ . '/../vendor/autoload.php';

session_start();

// Instantiate the app
$settings = require __DIR__ . '/../src/settings.php';
$app = new \Slim\App($settings);

// Set up dependencies
require __DIR__ . '/../src/dependencies.php';

// Register middleware
require __DIR__ . '/../src/middleware.php';

// Register routes
require __DIR__ . '/../src/routes.php';
require __DIR__ . '/../src/routes-sso.php';
require __DIR__ . '/../src/routes-api-misc.php';
require __DIR__ . '/../src/routes-api-market.php';
require __DIR__ . '/../src/routes-api-import.php';
require __DIR__ . '/../src/routes-api-lists.php';
require __DIR__ . '/../src/routes-api-wallet.php';
require __DIR__ . '/../src/routes-api-mer.php';
require __DIR__ . '/../src/routes-api-notifications.php';

// Run app
$app->run();
