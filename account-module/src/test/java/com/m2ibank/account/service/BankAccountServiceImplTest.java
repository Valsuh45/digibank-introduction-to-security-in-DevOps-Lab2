package com.m2ibank.account.service;

import com.m2ibank.account.dto.AccountRequestDto;
import com.m2ibank.account.dto.AccountResponseDto;
import com.m2ibank.account.entity.AccountStatus;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.entity.BankAccount;
import com.m2ibank.account.repository.BankAccountRepository;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the account service workflow.
 *
 * <p>These tests cover account creation, validation, lookup behavior, generated-number collision retry,
 * balance updates, and not-found handling. They use mocks so service rules can be checked without a real
 * database.</p>
 */
@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

    @Mock
    private BankAccountRepository repository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @Mock
    private CustomerService customerService;

    private BankAccountService service;

    @BeforeEach
    void setUp() {
        service = new BankAccountServiceImpl(repository, accountNumberGenerator, customerService);
    }

    @Test
    void createAccountPersistsAValidatedActiveXafAccount() {
        AccountRequestDto request = new AccountRequestDto(
                42L,
                AccountType.SAVINGS,
                new BigDecimal("1250.50")
        );
        when(accountNumberGenerator.generate()).thenReturn("123456789012");
        when(repository.findByAccountNumber("123456789012")).thenReturn(Optional.empty());
        when(repository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponseDto response = service.createAccount(request);

        verify(customerService).getCustomerById(42L);
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        org.mockito.Mockito.verify(repository).save(accountCaptor.capture());
        BankAccount persisted = accountCaptor.getValue();
        assertThat(persisted.getCustomerId()).isEqualTo(42L);
        assertThat(persisted.getBalance()).isEqualByComparingTo("1250.50");
        assertThat(persisted.getAccountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(persisted.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(persisted.getCurrency()).isEqualTo("XAF");
        assertThat(response.accountNumber()).isEqualTo("123456789012");
    }

    @Test
    void createAccountRetriesWhenGeneratedNumberAlreadyExists() {
        AccountRequestDto request = new AccountRequestDto(7L, AccountType.CURRENT, BigDecimal.ZERO);
        when(accountNumberGenerator.generate()).thenReturn("111111111111", "222222222222");
        when(repository.findByAccountNumber("111111111111")).thenReturn(Optional.of(account(1L, "111111111111")));
        when(repository.findByAccountNumber("222222222222")).thenReturn(Optional.empty());
        when(repository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponseDto response = service.createAccount(request);

        assertThat(response.accountNumber()).isEqualTo("222222222222");
    }

    @Test
    void createAccountRejectsNegativeBalanceEvenWhenControllerValidationIsBypassed() {
        AccountRequestDto request = new AccountRequestDto(7L, AccountType.CURRENT, new BigDecimal("-0.01"));

        assertThatThrownBy(() -> service.createAccount(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Initial balance must not be negative");
    }

    @Test
    void findByAccountNumberReturnsSafeResponse() {
        when(repository.findByAccountNumber("123456789012"))
                .thenReturn(Optional.of(account(9L, "123456789012")));

        AccountResponseDto response = service.findByAccountNumber("123456789012");

        assertThat(response.id()).isEqualTo(9L);
        assertThat(response.customerId()).isEqualTo(42L);
        assertThat(response.accountNumber()).isEqualTo("123456789012");
        assertThat(response.balance()).isEqualByComparingTo("1250.50");
    }

    @Test
    void listCustomerAccountsMapsAllMatchingAccounts() {
        when(repository.findByCustomerId(42L)).thenReturn(List.of(
                account(1L, "123456789012"),
                account(2L, "234567890123")
        ));

        List<AccountResponseDto> responses = service.getAccountsByCustomerId(42L);

        assertThat(responses).extracting(AccountResponseDto::id).containsExactly(1L, 2L);
    }

    @Test
    void getAccountDetailsUsesGenericNotFoundError() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAccountDetails(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bank account not found");
    }

    @Test
    void findByAccountNumberUsesGenericNotFoundError() {
        when(repository.findByAccountNumber("999999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByAccountNumber("999999999999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bank account not found");
    }

    @Test
    void updateBalancePersistsTheNewNonNegativeAmount() {
        BankAccount account = account(9L, "123456789012");
        when(repository.findByAccountNumber("123456789012")).thenReturn(Optional.of(account));

        service.updateBalance("123456789012", new BigDecimal("975.25"));

        assertThat(account.getBalance()).isEqualByComparingTo("975.25");
        verify(repository).save(account);
    }

    @Test
    void updateBalanceRejectsNegativeAmount() {
        BankAccount account = account(9L, "123456789012");
        when(repository.findByAccountNumber("123456789012")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> service.updateBalance("123456789012", new BigDecimal("-0.01")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Account balance must not be negative");
    }

    @Test
    void unknownCustomerIsRejectedBeforeGeneratingOrSavingAnAccount() {
        when(customerService.getCustomerById(404L))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        assertThatThrownBy(() -> service.createAccount(
                new AccountRequestDto(404L, AccountType.CURRENT, BigDecimal.ZERO)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");

        org.mockito.Mockito.verifyNoInteractions(repository, accountNumberGenerator);
    }

    @Test
    void balancePrecisionIsEnforcedByEntityEvenOutsideHttpValidation() {
        for (String amount : List.of("0.001", "100000000000000000", "1E+18")) {
            assertThatThrownBy(() -> BankAccount.open("123456789012", new BigDecimal(amount),
                    "XAF", AccountType.CURRENT, 42L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("17 integer digits and 2 decimal places");
        }
    }

    @Test
    void accountLookupTrimsSurroundingWhitespace() {
        when(repository.findByAccountNumber("123456789012"))
                .thenReturn(Optional.of(account(9L, "123456789012")));

        assertThat(service.findByAccountNumber(" 123456789012 ").id()).isEqualTo(9L);
    }

    @Test
    void invalidCreationRequestsNeverReachPersistence() {
        List<AccountRequestDto> invalid = java.util.Arrays.asList(
                null,
                new AccountRequestDto(null, AccountType.CURRENT, BigDecimal.ZERO),
                new AccountRequestDto(0L, AccountType.CURRENT, BigDecimal.ZERO),
                new AccountRequestDto(-1L, AccountType.CURRENT, BigDecimal.ZERO),
                new AccountRequestDto(1L, null, BigDecimal.ZERO),
                new AccountRequestDto(1L, AccountType.CURRENT, null));
        for (AccountRequestDto request : invalid) {
            assertThatThrownBy(() -> service.createAccount(request)).isInstanceOf(BusinessException.class);
        }
        org.mockito.Mockito.verifyNoInteractions(repository, accountNumberGenerator, customerService);
    }

    @Test
    void invalidIdentifiersNeverReachPersistence() {
        for (Long id : java.util.Arrays.asList(null, 0L, -1L)) {
            assertThatThrownBy(() -> service.getAccountDetails(id)).isInstanceOf(ResourceNotFoundException.class);
            assertThatThrownBy(() -> service.getBalance(id)).isInstanceOf(ResourceNotFoundException.class);
            assertThatThrownBy(() -> service.getAccountsByCustomerId(id)).isInstanceOf(BusinessException.class);
        }
        for (String number : java.util.Arrays.asList(null, "", "123", "000000000001", "12345678901x")) {
            assertThatThrownBy(() -> service.findByAccountNumber(number)).isInstanceOf(ResourceNotFoundException.class);
        }
        org.mockito.Mockito.verifyNoInteractions(repository);
    }

    @Test
    void exhaustedNumberGenerationFailsWithoutSaving() {
        when(accountNumberGenerator.generate()).thenReturn("123456789012");
        when(repository.findByAccountNumber("123456789012"))
                .thenReturn(Optional.of(account(1L, "123456789012")));
        assertThatThrownBy(() -> service.createAccount(new AccountRequestDto(1L, AccountType.CURRENT, BigDecimal.ZERO)))
                .isInstanceOf(BusinessException.class).hasMessage("Unable to create bank account");
        verify(accountNumberGenerator, org.mockito.Mockito.times(10)).generate();
        verify(repository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void accountDetailsAndBalanceReflectPersistedValues() {
        BankAccount persisted = account(1L, "123456789012");
        when(repository.findById(1L)).thenReturn(Optional.of(persisted));
        AccountResponseDto response = service.getAccountDetails(1L);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.currency()).isEqualTo("XAF");
        assertThat(response.accountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(response.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(persisted.getCreatedAt());
        assertThat(service.getBalance(1L)).isEqualByComparingTo("1250.50");
    }

    private BankAccount account(Long id, String accountNumber) {
        BankAccount account = BankAccount.open(
                accountNumber,
                new BigDecimal("1250.50"),
                "XAF",
                AccountType.SAVINGS,
                42L
        );
        ReflectionTestUtils.setField(account, "id", id);
        ReflectionTestUtils.setField(account, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));
        return account;
    }
}
