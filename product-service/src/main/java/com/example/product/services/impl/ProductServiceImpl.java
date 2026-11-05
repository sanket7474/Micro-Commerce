package com.example.product.services.impl;

import com.example.product.DTO.ProductDTO;
import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import com.example.product.services.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private ProductRepository repository;
    private ModelMapper modelMapper;

    public ProductServiceImpl(ProductRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ProductDTO> findAllProducts() {

        List<Product> products= repository.findAll();

        return  products.stream().map(item -> modelMapper.map(item, ProductDTO.class)).collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(Long id) {

        Optional<Product> product = repository.findById(id);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {

        Product product = modelMapper.map(productDTO, Product.class);
        product = repository.save(product);

        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, long id) {
        Product product = modelMapper.map(productDTO, Product.class);

        product.setId(id);
        product = repository.save(product);

        return modelMapper.map(product, ProductDTO.class);
    }
}
