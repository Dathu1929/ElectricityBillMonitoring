<?php
namespace App\Controllers;

use Exception;

class BoardsController {
    public function get_index(): void {
        try {
            $pdo = \Database::getConnection();
            $stmt = $pdo->query('SELECT * FROM electricity_boards ORDER BY id DESC');
            $boards = $stmt->fetchAll();
            json_response(['boards' => $boards]);
        } catch (Exception $e) {
            log_error('Boards fetch failed', ['message' => $e->getMessage()]);
            json_response(['error' => 'Server error'], 500);
        }
    }
}
