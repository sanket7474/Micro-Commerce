package com.example.customer.services.impl;

import com.example.customer.DTO.CustomerDTO;
import com.example.customer.model.Customer;
import com.example.customer.repository.CustomerRepository;
import com.example.customer.services.CustomerService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private CustomerRepository repository;
    private ModelMapper modelMapper;

    public CustomerServiceImpl(CustomerRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<CustomerDTO> findAllCustomers() {
        List<Customer> customers = repository.findAll();

        return customers.stream()
                .map(item -> modelMapper.map(item, CustomerDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO getCustomerById(Long id) {
        Optional<Customer> customer = repository.findById(id);
        return modelMapper.map(customer, CustomerDTO.class);
    }

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        Customer customer = modelMapper.map(customerDTO, Customer.class);
        customer = repository.save(customer);

        return modelMapper.map(customer, CustomerDTO.class);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO, long id) {
        Customer customer = modelMapper.map(customerDTO, Customer.class);

        customer.setId(id);
        customer = repository.save(customer);

        return modelMapper.map(customer, CustomerDTO.class);
    }
}
