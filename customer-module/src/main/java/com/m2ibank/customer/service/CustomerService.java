package com.m2ibank.customer.service;

import com.m2ibank.customer.dto.CustomerRequestDto;
import com.m2ibank.customer.dto.CustomerResponseDto;

import java.util.List;

/**
 * Application service contract for customer operations.
 *
 * <p>Controllers use this interface to create customers and retrieve customer views without depending on
 * the persistence model. Implementations own normalization, duplicate checks, and conversion from entity
 * objects to API DTOs.</p>
 */
public interface CustomerService {

    CustomerResponseDto createCustomer(CustomerRequestDto request);

    CustomerResponseDto getCustomerById(Long id);

    CustomerResponseDto getCustomerByEmail(String email);

    List<CustomerResponseDto> getAllCustomers();
}
