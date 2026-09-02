package com.m2ibank.customer.dto;

import com.m2ibank.customer.entity.CustomerStatus;

import java.time.Instant;

public record CustomerResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        CustomerStatus status,
        Instant createdAt) {
}
