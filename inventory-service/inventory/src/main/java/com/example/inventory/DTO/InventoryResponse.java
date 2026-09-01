package com.example.inventory.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class InventoryResponse {

    private int productId;
    private int availableQty;
    private int reservedQty;
}
