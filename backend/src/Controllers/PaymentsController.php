<?php
namespace App\Controllers;

use Exception;

class PaymentsController {
    public function get_index(): void {
        try {
            $pdo = \Database::getConnection();
            $stmt = $pdo->query('SELECT * FROM payments ORDER BY id DESC LIMIT 50');
            $payments = $stmt->fetchAll();
            json_response(['payments' => $payments]);
        } catch (Exception $e) {
            log_error('Payments fetch failed', ['message' => $e->getMessage()]);
            json_response(['error' => 'Server error'], 500);
        }
    }

    public function post_create(): void {
        try {
            $input = json_decode(file_get_contents('php://input'), true) ?? [];
            $billId = (int)($input['bill_id'] ?? 0);
            $amount = (float)($input['amount'] ?? 0);
            $method = trim($input['method'] ?? '');

            if ($billId <= 0 || $amount <= 0 || $method === '') {
                json_response(['error' => 'bill_id, amount, and method are required'], 400);
                return;
            }

            $pdo = \Database::getConnection();
            $stmt = $pdo->prepare('INSERT INTO payments (bill_id, amount, method, status, created_at) VALUES (:bill_id, :amount, :method, :status, NOW())');
            $stmt->execute([':bill_id' => $billId, ':amount' => $amount, ':method' => $method, ':status' => 'pending']);
            $paymentId = $pdo->lastInsertId();

            json_response(['message' => 'Payment record created', 'payment_id' => (int)$paymentId], 201);
        } catch (Exception $e) {
            log_error('Payment create failed', ['message' => $e->getMessage()]);
            json_response(['error' => 'Server error'], 500);
        }
    }
}
