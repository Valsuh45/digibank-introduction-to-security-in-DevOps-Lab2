package com.m2ibank.web.bdd;

import com.m2ibank.web.DigiBankApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring test configuration used by Cucumber scenarios.
 *
 * <p>This class connects Cucumber step definitions to the DigiBank Spring Boot application and activates
 * the test profile so scenarios use the in-memory database and test-safe settings.</p>
 */
@CucumberContextConfiguration
@SpringBootTest(classes = DigiBankApplication.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}
