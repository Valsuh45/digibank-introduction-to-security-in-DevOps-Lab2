package com.m2ibank.account.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {

    private static final long LOWER_BOUND = 100_000_000_000L;
    private static final long RANGE = 900_000_000_000L;

    private final SecureRandom secureRandom;

    public AccountNumberGenerator() {
        this(new SecureRandom());
    }

    AccountNumberGenerator(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    public String generate() {
        return Long.toString(LOWER_BOUND + secureRandom.nextLong(RANGE));
    }
}
