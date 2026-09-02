package com.m2ibank.web.bdd;

import com.m2ibank.web.DigiBankApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = DigiBankApplication.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}
