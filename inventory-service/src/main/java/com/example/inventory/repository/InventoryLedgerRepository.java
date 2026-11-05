package com.example.inventory.repository;

import com.example.inventory.model.InventoryLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLedgerRepository extends JpaRepository<InventoryLedger, Long> {

    // Debugging/audit endpoint: full reserve/release/commit history for a product
    List<InventoryLedger> findByProductIdOrderByCreatedAtDesc(Integer productId);

    // Useful when tracing a single order's saga
    List<InventoryLedger> findByOrderIdOrderByCreatedAtDesc(Integer orderId);
}
