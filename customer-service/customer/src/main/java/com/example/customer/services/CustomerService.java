package com.example.customer.services;

import com.example.customer.DTO.CustomerDTO;

import java.util.List;

public interface CustomerService {

    List<CustomerDTO> findAllCustomers();

    CustomerDTO getCustomerById(Long id);

    CustomerDTO createCustomer(CustomerDTO customer);

    CustomerDTO updateCustomer(CustomerDTO customer, long id);
}
