package com.ecommerce.payment.service.impl;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.entity.Payment;
import com.ecommerce.payment.entity.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentResponse charge(PaymentRequest request) {

        // Idempotency check: if order-service retries this call after a
        // timeout, return the existing result instead of charging again.
        var existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setAmount(request.getAmount());
        payment.setCurrency("INR");
        payment.setPaymentMethod(request.getPaymentMethod());

        // Mocked gateway call - no real payment provider wired up.
        // Swap this block out for an actual gateway integration later.
        boolean gatewaySuccess = true;

        if (gatewaySuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        payment = paymentRepository.save(payment);
        return toResponse(payment);
    }

    @Override
    public PaymentResponse getPayment(Integer id) {
        Payment payment = paymentRepository.findById(id).orElseThrow();
        return toResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentForOrder(Integer orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow();
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getTransactionRef()
        );
    }
}
