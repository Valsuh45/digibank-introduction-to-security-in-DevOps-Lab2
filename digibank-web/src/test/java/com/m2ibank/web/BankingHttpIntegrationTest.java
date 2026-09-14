package com.m2ibank.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2ibank.transfer.entity.Transfer;
import com.m2ibank.transfer.repository.TransferRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the assembled HTTP, validation, service, Flyway and persistence layers.
 * Tests intentionally have no enclosing test transaction: reads after a request observe committed
 * state, so automatic test rollback cannot conceal a broken service transaction.
 * The test profile uses H2; the companion Compose verification covers PostgreSQL separately.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:banking-http;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BankingHttpIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbc;
    @SpyBean
    private TransferRepository transfers;

    @Test
    void newlyCreatedCustomersCanOpenAccountsTransferAndReadTheirHistory() throws Exception {
        JsonNode customer = createCustomer();
        long customerId = customer.path("id").asLong();
        mvc.perform(get("/api/v1/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(customer.path("email").asText()));
        mvc.perform(get("/api/v1/customers/email/{email}", customer.path("email").asText()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(customerId));
        JsonNode source = createAccount(customerId, "500.00");
        JsonNode target = createAccount(createCustomer().path("id").asLong(), "100.00");
        String sourceNumber = source.path("accountNumber").asText();
        String targetNumber = target.path("accountNumber").asText();
        mvc.perform(get("/api/v1/accounts/{id}", source.path("id").asLong()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.accountNumber").value(sourceNumber));
        mvc.perform(get("/api/v1/accounts/customer/{id}", customerId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].accountNumber").value(sourceNumber));

        JsonNode transfer = create("/api/v1/transfers", transferBody(sourceNumber, targetNumber, "125.25"));
        mvc.perform(get("/api/v1/transfers/{id}", transfer.path("id").asLong()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.amount").value(125.25));
        assertBalance(sourceNumber, "374.75");
        assertBalance(targetNumber, "225.25");
        for (String number : new String[]{sourceNumber, targetNumber}) {
            mvc.perform(get("/api/v1/transfers/account/{number}", number))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].transferReference")
                            .value(transfer.path("transferReference").asText()));
        }
        mvc.perform(post("/api/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(transferBody(sourceNumber, targetNumber, "500.00"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Insufficient balance"));
        assertBalance(sourceNumber, "374.75");
        assertBalance(targetNumber, "225.25");
        assertThat(historySize(sourceNumber)).isEqualTo(1);
        assertThat(historySize(targetNumber)).isEqualTo(1);
    }

    @Test
    void persistenceFailureRollsBackAlreadyFlushedBalancesAndAuditRecord() throws Exception {
        String source = createAccount(createCustomer().path("id").asLong(), "500.00")
                .path("accountNumber").asText();
        String target = createAccount(createCustomer().path("id").asLong(), "100.00")
                .path("accountNumber").asText();
        long countBefore = transfers.count();
        doAnswer(invocation -> {
            // Inject a failure after SQL has written BOTH balance updates and the audit insert.
            // This is stronger than failing validation or checking only in-memory entities.
            Transfer transfer = invocation.getArgument(0);
            entityManager.persist(transfer);
            entityManager.flush();
            assertThat(jdbc.queryForObject("select balance from bank_accounts where account_number = ?",
                    BigDecimal.class, source)).isEqualByComparingTo("475.00");
            assertThat(jdbc.queryForObject("select balance from bank_accounts where account_number = ?",
                    BigDecimal.class, target)).isEqualByComparingTo("125.00");
            assertThat(jdbc.queryForObject("select count(*) from transfers", Long.class))
                    .isEqualTo(countBefore + 1);
            throw new IllegalStateException("Injected audit persistence failure");
        }).when(transfers).save(any(Transfer.class));

        mvc.perform(post("/api/v1/transfers").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(transferBody(source, target, "25.00"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(content().string(not(containsString("Injected audit"))));
        assertBalance(source, "500.00");
        assertBalance(target, "100.00");
        assertThat(transfers.count()).isEqualTo(countBefore);
        assertThat(historySize(source)).isZero();
        assertThat(historySize(target)).isZero();
    }

    @Test
    void frameworkClientErrorsKeepTheirHttpStatusAndSafeResponseEnvelope() throws Exception {
        mvc.perform(get("/api/v1/customers/not-a-number"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Request parameter is invalid"));
        mvc.perform(delete("/api/v1/customers"))
                .andExpect(status().isMethodNotAllowed()).andExpect(header().string("Allow", containsString("GET")))
                .andExpect(jsonPath("$.message").value("Method Not Allowed"));
        mvc.perform(post("/api/v1/customers").contentType(MediaType.TEXT_PLAIN).content("private input"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.message").value("Unsupported Media Type"));
        mvc.perform(get("/api/v1/missing-route"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Not Found"));
        mvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Request body is invalid"));
    }

    private JsonNode createCustomer() throws Exception {
        String unique = UUID.randomUUID().toString();
        return create("/api/v1/customers", Map.of("firstName", "Workshop", "lastName", "Learner",
                "email", unique + "@example.test", "identityNumber", unique));
    }

    private JsonNode createAccount(long customerId, String balance) throws Exception {
        return create("/api/v1/accounts", Map.of("customerId", customerId, "accountType", "CURRENT",
                "initialBalance", new BigDecimal(balance)));
    }

    private JsonNode create(String path, Map<String, ?> body) throws Exception {
        String response = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).path("data");
    }

    private Map<String, ?> transferBody(String source, String target, String amount) {
        return Map.of("sourceAccountNumber", source, "targetAccountNumber", target,
                "amount", new BigDecimal(amount), "description", "Workshop 1 HTTP evidence");
    }

    private void assertBalance(String number, String expected) throws Exception {
        String response = mvc.perform(get("/api/v1/accounts/number/{number}", number))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(mapper.readTree(response).path("data").path("balance").decimalValue())
                .isEqualByComparingTo(expected);
    }

    private int historySize(String number) throws Exception {
        String response = mvc.perform(get("/api/v1/transfers/account/{number}", number))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).path("data").size();
    }
}
