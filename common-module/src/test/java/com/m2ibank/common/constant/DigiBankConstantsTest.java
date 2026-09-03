package com.m2ibank.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for shared DigiBank constants.
 *
 * <p>This keeps important shared values, especially the default currency, under test so later edits do
 * not silently change API and persistence behavior.</p>
 */
class DigiBankConstantsTest {

    @Test
    void defaultCurrencyUsesCameroonCfaFrancCode() {
        assertEquals("XAF", DigiBankConstants.DEFAULT_CURRENCY);
    }
}
