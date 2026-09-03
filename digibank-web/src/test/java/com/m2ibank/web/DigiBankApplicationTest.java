package com.m2ibank.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Boot context test for the assembled application.
 *
 * <p>The empty test method is intentional: if the application context cannot start with the test profile,
 * this test fails before any endpoint-specific tests run.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class DigiBankApplicationTest {

    @Test
    void applicationContextStartsWithTheTestProfile() {
    }
}
