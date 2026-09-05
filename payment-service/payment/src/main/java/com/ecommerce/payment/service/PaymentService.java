package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;

public interface PaymentService {

    PaymentResponse charge(PaymentRequest request);

    PaymentResponse getPayment(Integer id);

    PaymentResponse getPaymentForOrder(Integer orderId);
}
