package com.example.customer.controller;

import com.example.customer.DTO.CustomerDTO;
import com.example.customer.services.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("")
    private List<CustomerDTO> getAllCustomers() {
        return customerService.findAllCustomers();
    }

    @GetMapping("/{id}")
    private CustomerDTO getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @PostMapping("")
    private ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customer) {
        CustomerDTO value = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(value);
    }

    @PutMapping("/{id}")
    private ResponseEntity<CustomerDTO> updateCustomer(@RequestBody CustomerDTO customer,
                                                        @PathVariable long id) {
        CustomerDTO value = customerService.updateCustomer(customer, id);
        return ResponseEntity.status(HttpStatus.OK).body(value);
    }
}
