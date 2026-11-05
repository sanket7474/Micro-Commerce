package com.example.inventory.controller;

import com.example.inventory.DTO.InventoryResponse;
import com.example.inventory.DTO.StockOperationRequest;
import com.example.inventory.model.InventoryLedger;
import com.example.inventory.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // GET /api/v1/inventory/{productId} - public read, called by clients/other services
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getStock(@PathVariable Integer productId) {
        return ResponseEntity.ok(inventoryService.getStock(productId));
    }

    // POST /api/v1/inventory/{productId}/reserve - called by order-service during checkout
    @PostMapping("/{productId}/reserve")
    public ResponseEntity<Void> reserve(@PathVariable Integer productId,
                                        @RequestBody StockOperationRequest request) {
        inventoryService.reserveStock(productId, request.getOrderId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    // POST /api/v1/inventory/{productId}/release - compensating action if payment fails
    @PostMapping("/{productId}/release")
    public ResponseEntity<Void> release(@PathVariable Integer productId,
                                        @RequestBody StockOperationRequest request) {
        inventoryService.releaseStock(productId, request.getOrderId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    // POST /api/v1/inventory/{productId}/commit - called once payment succeeds
    @PostMapping("/{productId}/commit")
    public ResponseEntity<Void> commit(@PathVariable Integer productId,
                                        @RequestBody StockOperationRequest request) {
        inventoryService.commitStock(productId, request.getOrderId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    // GET /api/v1/inventory/{productId}/ledger - debugging/audit view, admin-only in practice
    @GetMapping("/{productId}/ledger")
    public ResponseEntity<List<InventoryLedger>> getLedger(@PathVariable Integer productId) {
        return ResponseEntity.ok(inventoryService.getLedgerForProduct(productId));
    }

}
