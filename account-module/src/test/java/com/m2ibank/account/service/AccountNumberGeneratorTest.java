package com.m2ibank.account.service;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for generated account-number format.
 *
 * <p>The test confirms generated account numbers are 12 digits and do not start with zero. The generator
 * uses secure randomness; this test focuses on the public format contract.</p>
 */
class AccountNumberGeneratorTest {

    @Test
    void generatedAccountNumberContainsExactlyTwelveDigits() {
        AccountNumberGenerator generator = new AccountNumberGenerator(new SecureRandom());

        String accountNumber = generator.generate();

        assertThat(accountNumber).matches("[1-9][0-9]{11}");
    }
}
