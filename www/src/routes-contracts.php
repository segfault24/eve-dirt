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

$app->get('/contracts-finished', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
        ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);
    
    return $this->renderer->render($response, 'contracts-finished.phtml', $args);
});

$app->get('/contract', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isLoggedIn()) {
        return $response->withStatus(302)
            ->withHeader('Location', '/login');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'contract.phtml', $args);
});
