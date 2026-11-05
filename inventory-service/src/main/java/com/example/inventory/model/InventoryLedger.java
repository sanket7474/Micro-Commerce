package com.example.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="inventory_ledger")
@Getter
@Setter
public class InventoryLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    // Logical reference to order_db.customer_order.id
    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private ChangeType changeType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;


}
