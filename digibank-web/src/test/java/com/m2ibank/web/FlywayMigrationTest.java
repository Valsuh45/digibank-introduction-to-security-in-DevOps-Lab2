package com.m2ibank.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void migrationsCreateAndSeedTheBankingSchema() {
        List<String> versions = jdbcTemplate.queryForList(
                "select version from flyway_schema_history where success = true and version is not null order by installed_rank",
                String.class);

        Integer customers = jdbcTemplate.queryForObject("select count(*) from customers", Integer.class);
        Integer accounts = jdbcTemplate.queryForObject("select count(*) from bank_accounts", Integer.class);
        Integer transfers = jdbcTemplate.queryForObject("select count(*) from transfers", Integer.class);
        Integer linkedAccounts = jdbcTemplate.queryForObject(
                "select count(*) from bank_accounts a join customers c on c.id = a.customer_id",
                Integer.class);

        assertThat(versions).containsExactly("1", "2");
        assertThat(customers).isEqualTo(2);
        assertThat(accounts).isEqualTo(2);
        assertThat(transfers).isEqualTo(1);
        assertThat(linkedAccounts).isEqualTo(2);
    }
}
