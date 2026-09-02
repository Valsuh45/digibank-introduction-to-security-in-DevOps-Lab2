package com.m2ibank.customer.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void acceptsValidCustomerInput() {
        CustomerRequestDto request = new CustomerRequestDto("Jane", "Doe", "jane@example.com", "ID-12345");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsBlankNamesInvalidEmailAndMissingIdentityNumber() {
        CustomerRequestDto request = new CustomerRequestDto(" ", "", "not-an-email", null);

        Set<ConstraintViolation<CustomerRequestDto>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("firstName", "lastName", "email", "identityNumber");
    }

    @Test
    void rejectsOversizedInput() {
        CustomerRequestDto request = new CustomerRequestDto(
                "a".repeat(101), "b".repeat(101), "c".repeat(245) + "@example.com", "i".repeat(101));

        Set<ConstraintViolation<CustomerRequestDto>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("firstName", "lastName", "email", "identityNumber");
    }
}
