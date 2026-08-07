<?php
namespace App\Services;

class PaymentService {
    public function createPayment(array $payload): array {
        return [
            'provider' => $payload['provider'] ?? 'upi',
            'status' => 'pending',
            'provider_reference' => 'ref-' . bin2hex(random_bytes(4))
        ];
    }

    public function verifyPayment(string $provider, string $reference): array {
        return [
            'provider' => $provider,
            'reference' => $reference,
            'verified' => true,
            'status' => 'captured'
        ];
    }
}
