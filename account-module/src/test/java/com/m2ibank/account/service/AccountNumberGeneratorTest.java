package com.m2ibank.account.service;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

class AccountNumberGeneratorTest {

    @Test
    void generatedAccountNumberContainsExactlyTwelveDigits() {
        AccountNumberGenerator generator = new AccountNumberGenerator(new SecureRandom());

        String accountNumber = generator.generate();

        assertThat(accountNumber).matches("[1-9][0-9]{11}");
    }
}
