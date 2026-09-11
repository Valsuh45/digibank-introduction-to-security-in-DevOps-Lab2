package com.m2ibank.customer.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validation tests for customer registration requests.
 *
 * <p>The tests confirm that valid customer data passes and missing, malformed, or overlong fields fail
 * before the request reaches the service layer.</p>
 */
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

    @Test
    void rejectsMalformedNamesAndIdentityNumbers() {
        CustomerRequestDto request = new CustomerRequestDto(
                "J4ne", "D@e", "jane@example.com", "ID/123");

        Set<ConstraintViolation<CustomerRequestDto>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("firstName", "lastName", "identityNumber");
    }

    @Test
    void rejectsNamesThatBecomeTooShortAfterTrimming() {
        CustomerRequestDto request = new CustomerRequestDto(
                "J ", "D ", "jane@example.com", "ID-12345");

        Set<ConstraintViolation<CustomerRequestDto>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("firstName", "lastName");
    }

    @Test
    void rejectsNamesWithConsecutiveSeparators() {
        // Reject double hyphens, double dots, mixed separators
        CustomerRequestDto requestDoubleHyphen = new CustomerRequestDto(
                "Jean--Paul", "Smith", "jane@example.com", "ID-12345");
        CustomerRequestDto requestDoubleDot = new CustomerRequestDto(
                "Mary", "O..Brien", "jane@example.com", "ID-12345");
        CustomerRequestDto requestMixedSeparators = new CustomerRequestDto(
                "John", "O'- Brien", "jane@example.com", "ID-12345");

        assertThat(validator.validate(requestDoubleHyphen))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("firstName");
        assertThat(validator.validate(requestDoubleDot))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("lastName");
        assertThat(validator.validate(requestMixedSeparators))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("lastName");
    }

    @Test
    void acceptsNamesWithSingleSeparatorsBetweenLetters() {
        // Accept valid hyphenated, dotted, and apostrophed names
        CustomerRequestDto requestHyphenated = new CustomerRequestDto(
                "Jean-Paul", "Smith", "jane@example.com", "ID-12345");
        CustomerRequestDto requestDotted = new CustomerRequestDto(
                "Mary", "O.Brien", "jane@example.com", "ID-12345");
        CustomerRequestDto requestApostrophed = new CustomerRequestDto(
                "John", "O'Brien", "jane@example.com", "ID-12345");
        CustomerRequestDto requestSpaced = new CustomerRequestDto(
                "Mary Ann", "van der Berg", "jane@example.com", "ID-12345");

        assertThat(validator.validate(requestHyphenated)).isEmpty();
        assertThat(validator.validate(requestDotted)).isEmpty();
        assertThat(validator.validate(requestApostrophed)).isEmpty();
        assertThat(validator.validate(requestSpaced)).isEmpty();
    }
}
