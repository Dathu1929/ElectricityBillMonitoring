<?php
require_once __DIR__ . '/../config.php';
require_once __DIR__ . '/../database.php';

// Handle CORS
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS, PUT, DELETE");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') {
    exit;
}

$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
// Correctly handle subfolder installations
$scriptName = $_SERVER['SCRIPT_NAME'];
$basePath = str_replace('/index.php', '', $scriptName);
$path = substr($uri, strlen($basePath));

$segments = array_values(array_filter(explode('/', trim($path, '/'))));

if (empty($segments)) {
    json_response(['message' => 'Electricity Bill Monitoring API is running']);
    exit;
}

$controllerName = ucfirst($segments[0]) . 'Controller';
$controllerFile = __DIR__ . '/../src/Controllers/' . $controllerName . '.php';

if (!file_exists($controllerFile)) {
    json_response(['message' => 'Route not found: ' . $controllerName], 404);
    exit;
}

require_once $controllerFile;
$className = 'App\\Controllers\\' . $controllerName;
$controller = new $className();

$method = strtolower($_SERVER['REQUEST_METHOD']);
$action = $segments[1] ?? null;

$callable = null;
if ($action === null) {
    $callable = [$controller, $method . '_index'];
    if (!is_callable($callable)) {
        $callable = [$controller, 'index'];
    }
} else {
    $callable = [$controller, $method . '_' . $action];
}

if (is_callable($callable)) {
    $callable();
} else {
    json_response(['message' => 'Action not found: ' . $method . '_' . ($action ?? 'index')], 404);
}
