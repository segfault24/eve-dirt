<?php
// Application middleware

$app->add(new \Slim\HttpCache\Cache('public', 86400));
