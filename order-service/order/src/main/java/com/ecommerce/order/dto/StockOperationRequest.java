package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StockOperationRequest {
    private Integer orderId;
    private Integer quantity;
}
