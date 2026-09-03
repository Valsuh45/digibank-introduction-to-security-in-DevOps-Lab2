package com.m2ibank.account.dto;

import com.m2ibank.account.entity.AccountType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validation tests for account creation requests.
 *
 * <p>These tests prove that valid account requests pass and invalid balances or missing fields are
 * rejected before the service creates a bank account.</p>
 */
class AccountRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void missingRequiredFieldsAreRejected() {
        AccountRequestDto request = new AccountRequestDto(null, null, null);

        Set<ConstraintViolation<AccountRequestDto>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("customerId", "accountType", "initialBalance");
    }

    @Test
    void negativeInitialBalanceIsRejected() {
        AccountRequestDto request = new AccountRequestDto(
                1L,
                AccountType.CURRENT,
                new BigDecimal("-0.01")
        );

        Set<ConstraintViolation<AccountRequestDto>> violations = validator.validate(request);

        assertThat(violations).singleElement()
                .satisfies(violation -> assertThat(violation.getPropertyPath().toString())
                        .isEqualTo("initialBalance"));
    }
}
