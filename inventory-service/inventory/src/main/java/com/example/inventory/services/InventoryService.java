package com.example.inventory.services;

import com.example.inventory.DTO.InventoryResponse;
import com.example.inventory.model.InventoryLedger;

import java.util.List;

public interface InventoryService {

    InventoryResponse getStock(Integer productId);

    void reserveStock(Integer productId, Integer orderId, Integer quantity);

    void releaseStock(Integer productId, Integer orderId, Integer quantity);

    void commitStock(Integer productId, Integer orderId, Integer quantity);

    List<InventoryLedger> getLedgerForProduct(Integer productId);
}
