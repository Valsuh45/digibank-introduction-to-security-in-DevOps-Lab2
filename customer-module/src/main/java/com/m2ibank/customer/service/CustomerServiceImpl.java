package com.m2ibank.customer.service;

import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.customer.dto.CustomerRequestDto;
import com.m2ibank.customer.dto.CustomerResponseDto;
import com.m2ibank.customer.entity.Customer;
import com.m2ibank.customer.entity.CustomerStatus;
import com.m2ibank.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Default customer service implementation.
 *
 * <p>This class creates and reads customers. It normalizes email addresses, trims identity numbers and
 * names, checks for duplicate email and identity values, and maps saved entities into response DTOs.</p>
 *
 * <p>The database unique constraints are treated as a second safety layer. If a race condition gets past
 * the pre-save checks, the constraint exception is caught and converted into a clear business error
 * without leaking database details to API clients.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto request) {
        String email = normalizeEmail(request.email());
        String identityNumber = request.identityNumber().trim();

        if (customerRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("A customer with this email already exists");
        }
        if (customerRepository.findByIdentityNumber(identityNumber).isPresent()) {
            throw new BusinessException("A customer with this identity number already exists");
        }

        Customer customer = Customer.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .identityNumber(identityNumber)
                .status(CustomerStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        try {
            return toResponse(customerRepository.saveAndFlush(customer));
        } catch (DataIntegrityViolationException exception) {
            log.warn("Customer creation rejected by a database constraint");
            throw new BusinessException("A customer with the supplied details already exists");
        }
    }

    @Override
    public CustomerResponseDto getCustomerById(Long id) {
        return customerRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Override
    public CustomerResponseDto getCustomerByEmail(String email) {
        return customerRepository.findByEmail(normalizeEmail(email))
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Override
    public List<CustomerResponseDto> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private CustomerResponseDto toResponse(Customer customer) {
        return new CustomerResponseDto(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getStatus(),
                customer.getCreatedAt());
    }
}
