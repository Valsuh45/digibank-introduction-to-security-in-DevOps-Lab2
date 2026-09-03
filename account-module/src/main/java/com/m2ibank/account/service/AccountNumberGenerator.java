package com.m2ibank.account.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates 12-digit account numbers for newly opened accounts.
 *
 * <p>The generator uses {@link SecureRandom} instead of predictable random sources. Account numbers are
 * not passwords, but predictable identifiers can still help attackers enumerate accounts, so secure
 * randomness is a sensible default.</p>
 *
 * <p>The service layer checks generated candidates against the repository before saving. This class only
 * creates candidates; it does not know about persistence or uniqueness.</p>
 */
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
