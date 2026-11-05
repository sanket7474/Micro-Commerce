package com.example.inventory.DTO;


import lombok.Getter;
import lombok.Setter;

/**
 * Body for POST /api/v1/inventory/{productId}/reserve|release|commit
 * Called by order-service during the order-placement saga.
 */
@Getter
@Setter
public class StockOperationRequest {

    private Integer orderId;
    private Integer quantity;
}
