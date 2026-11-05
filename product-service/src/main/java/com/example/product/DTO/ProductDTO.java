package com.example.product.DTO;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class ProductDTO {

    private String sku;

    
    private String name;

    
    private String description;

    
    private String category;

    
    private Double price;

    
    private short is_active;
}
