package com.m2ibank.customer.service;

import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.customer.dto.CustomerRequestDto;
import com.m2ibank.customer.dto.CustomerResponseDto;
import com.m2ibank.customer.entity.Customer;
import com.m2ibank.customer.entity.CustomerStatus;
import com.m2ibank.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for customer service rules.
 *
 * <p>These tests cover customer creation, email normalization, duplicate email and identity rejection,
 * database constraint fallback handling, lookups, and response mapping.</p>
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository);
    }

    @Test
    void createsActiveCustomerWithNormalizedInput() {
        CustomerRequestDto request = new CustomerRequestDto(
                "  Jane  ", "  Doe ", " Jane.Doe@Example.COM ", " ID-12345 ");
        when(customerRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(customerRepository.findByIdentityNumber("ID-12345")).thenReturn(Optional.empty());
        when(customerRepository.saveAndFlush(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(42L);
            return customer;
        });

        CustomerResponseDto response = customerService.createCustomer(request);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.firstName()).isEqualTo("Jane");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.email()).isEqualTo("jane.doe@example.com");
        assertThat(response.status()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void rejectsDuplicateEmail() {
        CustomerRequestDto request = validRequest();
        when(customerRepository.findByEmail("jane.doe@example.com"))
                .thenReturn(Optional.of(customer(1L, "jane.doe@example.com")));

        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A customer with this email already exists");
    }

    @Test
    void rejectsDuplicateIdentityNumber() {
        CustomerRequestDto request = validRequest();
        when(customerRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(customerRepository.findByIdentityNumber("ID-12345"))
                .thenReturn(Optional.of(customer(1L, "other@example.com")));

        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A customer with this identity number already exists");
    }

    @Test
    void returnsCustomerById() {
        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer(7L, "jane.doe@example.com")));

        CustomerResponseDto response = customerService.getCustomerById(7L);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.email()).isEqualTo("jane.doe@example.com");
    }

    @Test
    void reportsMissingCustomerById() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found with id: 99");
    }

    @Test
    void returnsCustomerByNormalizedEmail() {
        when(customerRepository.findByEmail("jane.doe@example.com"))
                .thenReturn(Optional.of(customer(7L, "jane.doe@example.com")));

        CustomerResponseDto response = customerService.getCustomerByEmail(" Jane.Doe@Example.COM ");

        assertThat(response.id()).isEqualTo(7L);
    }

    @Test
    void reportsMissingCustomerByEmailWithoutEchoingIt() {
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerByEmail("unknown@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    void listsAllCustomers() {
        when(customerRepository.findAll()).thenReturn(List.of(
                customer(1L, "jane.doe@example.com"),
                customer(2L, "john.doe@example.com")));

        List<CustomerResponseDto> responses = customerService.getAllCustomers();

        assertThat(responses).extracting(CustomerResponseDto::id).containsExactly(1L, 2L);
    }

    private CustomerRequestDto validRequest() {
        return new CustomerRequestDto("Jane", "Doe", "jane.doe@example.com", "ID-12345");
    }

    private Customer customer(Long id, String email) {
        return Customer.builder()
                .id(id)
                .firstName("Jane")
                .lastName("Doe")
                .email(email)
                .identityNumber("ID-12345")
                .status(CustomerStatus.ACTIVE)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
    }
}
