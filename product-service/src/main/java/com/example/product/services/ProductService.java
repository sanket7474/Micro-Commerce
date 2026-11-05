package com.example.product.services;

import com.example.product.DTO.ProductDTO;

import java.util.List;

public interface ProductService {

    List<ProductDTO> findAllProducts();


    ProductDTO getProductById(Long id);

    ProductDTO createProduct(ProductDTO product);

    ProductDTO updateProduct(ProductDTO product, long id);
}
