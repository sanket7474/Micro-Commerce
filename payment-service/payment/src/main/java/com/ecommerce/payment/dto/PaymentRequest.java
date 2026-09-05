package com.ecommerce.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {
    private Integer orderId;
    private String idempotencyKey;
    private BigDecimal amount;
    private String paymentMethod;
}
