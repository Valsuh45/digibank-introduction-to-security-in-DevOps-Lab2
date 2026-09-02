package com.m2ibank.transfer.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TransferRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsUnsafeTransferInput() {
        TransferRequestDto request = new TransferRequestDto(
                " ",
                "",
                BigDecimal.ZERO,
                "x".repeat(256));

        Set<ConstraintViolation<TransferRequestDto>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder(
                        "sourceAccountNumber",
                        "targetAccountNumber",
                        "amount",
                        "description");
    }

    @Test
    void acceptsValidTransferInputWithoutDescription() {
        TransferRequestDto request = new TransferRequestDto(
                "ACC-001",
                "ACC-002",
                new BigDecimal("25.00"),
                null);

        assertThat(validator.validate(request)).isEmpty();
    }
}
