package com.m2ibank.customer.service;

import com.m2ibank.customer.dto.CustomerRequestDto;
import com.m2ibank.customer.dto.CustomerResponseDto;

import java.util.List;

public interface CustomerService {

    CustomerResponseDto createCustomer(CustomerRequestDto request);

    CustomerResponseDto getCustomerById(Long id);

    CustomerResponseDto getCustomerByEmail(String email);

    List<CustomerResponseDto> getAllCustomers();
}
