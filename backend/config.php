<?php
require_once __DIR__ . '/vendor/autoload.php';

$env = []; 
if (file_exists(__DIR__ . '/.env')) {
    foreach (file(__DIR__ . '/.env', FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES) as $line) {
        if (str_starts_with($line, '#')) continue;
        [$name, $value] = array_pad(explode('=', $line, 2), 2, '');
        $env[trim($name)] = trim($value);
    }
}

function env(string $key, $default = null) {
    global $env;
    return $env[$key] ?? $_ENV[$key] ?? $_SERVER[$key] ?? $default;
}

function json_response($payload, int $status = 200): void {
    http_response_code($status);
    header('Content-Type: application/json');
    echo json_encode($payload, JSON_UNESCAPED_SLASHES);
}

function log_error(string $message, array $context = []): void {
    $path = env('LOG_PATH', __DIR__ . '/storage/app.log');
    $entry = '[' . date('Y-m-d H:i:s') . '] ' . $message . ' ' . json_encode($context) . PHP_EOL;
    file_put_contents($path, $entry, FILE_APPEND);
}
