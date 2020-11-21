<?php
// Application middleware

$app->add(new \Slim\HttpCache\Cache('private', 1200));
