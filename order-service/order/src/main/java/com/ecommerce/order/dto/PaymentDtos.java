package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class PaymentDtos {

    @Getter
    @AllArgsConstructor
    public static class PaymentRequest {
        private Integer orderId;
        private String idempotencyKey;
        private BigDecimal amount;
        private String paymentMethod;
    }

    @Getter
    @Setter
    public static class PaymentResponse {
        private String status; // SUCCESS / FAILED
    }
}
