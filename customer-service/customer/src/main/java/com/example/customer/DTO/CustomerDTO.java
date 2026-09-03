package com.example.customer.DTO;

import lombok.Data;

@Data
public class CustomerDTO {

    private long user_id;
    private String full_name;
    private String phone;
    private String address_line1;
    private String address_line2;
    private String city;
    private String state;
    private String postal_code;
    private String country;
}
