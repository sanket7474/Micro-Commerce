package com.example.product.controller;

import com.example.product.DTO.ProductDTO;
import com.example.product.services.ProductService;
import jakarta.websocket.server.PathParam;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("")
    private List<ProductDTO> getAllProducts() {
        return productService.findAllProducts();
    }

    @GetMapping("/{id}")
    private ProductDTO getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping("")
    private ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO product) {
        ProductDTO value = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(value);
    }

    @PutMapping("/{id}")
    private ResponseEntity<ProductDTO> updateProduct(@RequestBody ProductDTO product, @PathVariable long id) {
        ProductDTO value = productService.updateProduct(product, id);
        return ResponseEntity.status(HttpStatus.OK).body(value);
    }
}
