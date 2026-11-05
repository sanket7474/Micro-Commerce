package com.example.inventory.repository;

import com.example.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    Optional<Inventory> findByProductId(Integer productId);

    /**
     * Atomically moves qty from available -> reserved, but ONLY if enough
     * stock exists. The WHERE clause and the decrement happen as a single
     * DB statement, so two concurrent orders can't both pass a separate
     * "check stock" step before either one commits - this is what
     * actually prevents overselling under concurrent requests.
     *
     * Returns the number of rows updated: 1 = reservation succeeded,
     * 0 = not enough available stock (the WHERE condition failed).
     */
    @Modifying
    @Query("""
           UPDATE Inventory i
           SET i.availableQty = i.availableQty - :qty,
               i.reservedQty  = i.reservedQty + :qty
           WHERE i.productId = :productId
             AND i.availableQty >= :qty
           """)
    int reserveStock(@Param("productId") Integer productId, @Param("qty") Integer qty);

    /**
     * Compensating action: moves qty back from reserved -> available.
     * Called when a later saga step (e.g. payment) fails after a
     * reservation already succeeded.
     */
    @Modifying
    @Query("""
           UPDATE Inventory i
           SET i.availableQty = i.availableQty + :qty,
               i.reservedQty  = i.reservedQty - :qty
           WHERE i.productId = :productId
             AND i.reservedQty >= :qty
           """)
    int releaseStock(@Param("productId") Integer productId, @Param("qty") Integer qty);

    /**
     * Called once payment succeeds - the reserved stock is now
     * permanently gone (actually sold), not just held.
     */
    @Modifying
    @Query("""
           UPDATE Inventory i
           SET i.reservedQty = i.reservedQty - :qty
           WHERE i.productId = :productId
             AND i.reservedQty >= :qty
           """)
    int commitStock(@Param("productId") Integer productId, @Param("qty") Integer qty);
}
