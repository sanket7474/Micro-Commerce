package com.ecommerce.order.dto;

import com.ecommerce.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private Integer orderId;
    private OrderStatus status;
    private BigDecimal totalAmount;
}
