<?php
namespace App\Controllers;

use Exception;

class AiController {
    public function get_predict(): void {
        try {
            $pdo = \Database::getConnection();
            $stmt = $pdo->query('SELECT AVG(usage_kwh) AS avg_usage, MAX(usage_kwh) AS max_usage FROM usage_history');
            $stats = $stmt->fetch();
            $prediction = [
                'estimated_next_bill' => round(($stats['avg_usage'] ?? 0) * 1.2, 2),
                'trend' => (($stats['avg_usage'] ?? 0) > 250) ? 'high' : 'stable',
            ];
            json_response(['prediction' => $prediction]);
        } catch (Exception $e) {
            log_error('AI prediction failed', ['message' => $e->getMessage()]);
            json_response(['error' => 'Server error'], 500);
        }
    }
}
