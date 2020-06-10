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

$app->get('/admin/create-user', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isAdmin()) {
        $this->logger->warning('/admin/create-user unauthorized access attempt');
        return $response->withStatus(302)
        ->withHeader('Location', '/dashboard');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'admin/create-user.phtml', $args);
});

$app->post('/admin/create-user', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isAdmin()) {
        $this->logger->warning('/admin/create-user unauthorized access attempt');
        return $response->withStatus(302)
        ->withHeader('Location', '/dashboard');
    }

    $usernm = $request->getParsedBody()['usernm'];
    $userpw = $request->getParsedBody()['userpw'];
    $userpwconf = $request->getParsedBody()['userpwconf'];
    $admin = false;

    $err = Dirt\Tools::createUser($usernm, $userpw, $userpwconf, $admin);
    if (empty($err)) {
        $this->logger->info('/admin/create-user created user ' . $usernm);
        $args['successmsg'] = "Successfully created user";
    } else {
        $this->logger->info('/admin/create-user failed to create user: ' . $err);
        $args['errormsg'] = $err;
    }

    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'admin/create-user.phtml', $args);
});

$app->get('/admin/list-users', function ($request, $response, $args) {
    $u = Dirt\User::getUser();
    if (! $u->isAdmin()) {
        $this->logger->warning('/admin/list-users unauthorized access attempt');
        return $response->withStatus(302)
        ->withHeader('Location', '/dashboard');
    }
    $u->setTemplateVars($args);

    return $this->renderer->render($response, 'admin/list-users.phtml', $args);
});

