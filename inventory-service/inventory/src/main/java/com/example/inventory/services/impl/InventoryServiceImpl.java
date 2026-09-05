package com.example.inventory.services.impl;

import com.example.inventory.DTO.InventoryResponse;
import com.example.inventory.model.ChangeType;
import com.example.inventory.model.Inventory;
import com.example.inventory.model.InventoryLedger;
import com.example.inventory.repository.InventoryLedgerRepository;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;

    @Override
    public InventoryResponse getStock(Integer productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("" + productId));
        return new InventoryResponse(
                inventory.getProductId(),
                inventory.getAvailableQty(),
                inventory.getReservedQty()
        );
    }

    @Override
    @Transactional
    public void reserveStock(Integer productId, Integer orderId, Integer quantity) {
        int rowsUpdated = inventoryRepository.reserveStock(productId, quantity);
        if (rowsUpdated == 0) {
            // Either the product doesn't exist, or there wasn't enough
            // available stock - both are treated as a 409 to the caller.
            throw new RuntimeException("Not enough stock available for product " + productId);
        }
        writeLedgerEntry(productId, orderId, ChangeType.RESERVE, quantity);
    }

    @Override
    @Transactional
    public void releaseStock(Integer productId, Integer orderId, Integer quantity) {
        int rowsUpdated = inventoryRepository.releaseStock(productId, quantity);
        if (rowsUpdated == 0) {
            throw new RuntimeException("" + productId);
        }
        writeLedgerEntry(productId, orderId, ChangeType.RELEASE, quantity);
    }

    @Override
    @Transactional
    public void commitStock(Integer productId, Integer orderId, Integer quantity) {
        int rowsUpdated = inventoryRepository.commitStock(productId, quantity);
        if (rowsUpdated == 0) {
            throw new RuntimeException("" + productId);
        }
        writeLedgerEntry(productId, orderId, ChangeType.COMMIT, quantity);
    }

    @Override
    public List<InventoryLedger> getLedgerForProduct(Integer productId) {
        return inventoryLedgerRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    private void writeLedgerEntry(Integer productId, Integer orderId, ChangeType type, Integer qty) {
        InventoryLedger entry = new InventoryLedger();
        entry.setProductId(productId);
        entry.setOrderId(orderId);
        entry.setChangeType(type);
        entry.setQuantity(qty);
        inventoryLedgerRepository.save(entry);
    }
}
