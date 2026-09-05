package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// server.servlet.context-path = /api/v1, so this maps to /api/v1/payments/**
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> charge(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.charge(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Integer id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentForOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(paymentService.getPaymentForOrder(orderId));
    }
}
