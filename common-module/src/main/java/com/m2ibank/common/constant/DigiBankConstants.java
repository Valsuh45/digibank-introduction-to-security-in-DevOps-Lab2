package com.m2ibank.common.constant;

/**
 * Shared constants used across DigiBank modules.
 *
 * <p>This class currently stores values that are intentionally common to several domains. The default
 * currency is kept here so account creation, transfer responses, tests, and documentation all use the
 * same XAF value instead of repeating a magic string.</p>
 *
 * <p>The private constructor prevents accidental instantiation. This class is only a namespace for
 * constants and should not hold mutable state.</p>
 */
public final class DigiBankConstants {

    public static final String DEFAULT_CURRENCY = "XAF";

    private DigiBankConstants() {
    }
}
