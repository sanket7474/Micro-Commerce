package com.example.customer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customer")
@Getter
@Setter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private long user_id;

    @Column(nullable = false)
    private String full_name;

    private String phone;
    private String address_line1;
    private String address_line2;
    private String city;
    private String state;
    private String postal_code;
    private String country;
}
