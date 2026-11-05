package com.example.inventory.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name= "inventory")
@Getter
@Setter
@Entity
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // Logical reference to product_db.product.id - no cross-DB FK,
    // integrity is enforced in application code, not the database.
    @Column(name = "product_id", nullable = false, unique = true)
    private Integer productId;

    @Column(name = "available_qty", nullable = false)
    private Integer availableQty;

    @Column(name = "reserved_qty", nullable = false)
    private Integer reservedQty;

}
