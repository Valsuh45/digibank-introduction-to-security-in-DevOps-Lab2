package com.m2ibank.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DigiBankConstantsTest {

    @Test
    void defaultCurrencyUsesCameroonCfaFrancCode() {
        assertEquals("XAF", DigiBankConstants.DEFAULT_CURRENCY);
    }
}
