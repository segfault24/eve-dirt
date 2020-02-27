<?php
use Dirt\Tools;

// //////////////////////////////////////////////
// // Admin Pages ////
// //////////////////////////////////////////////

$app->get('/admin', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isAdmin()) {
        $this->logger->warning('/admin unauthorized access attempt');
        return $response->withStatus(302)
            ->withHeader('Location', '/dashboard');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'admin/index.phtml', $args);
});

$app->get('/admin/test', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isAdmin()) {
        $this->logger->warning('/admin/test unauthorized access attempt');
        return $response->withStatus(302)
            ->withHeader('Location', '/dashboard');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'admin/test.phtml', $args);
});
