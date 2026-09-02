package com.m2ibank.account.service;

import com.m2ibank.account.dto.AccountRequestDto;
import com.m2ibank.account.dto.AccountResponseDto;
import com.m2ibank.account.entity.AccountStatus;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.entity.BankAccount;
import com.m2ibank.account.repository.BankAccountRepository;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
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

@ExtendWith(MockitoExtension.class)
class BankAccountServiceImplTest {

    @Mock
    private BankAccountRepository repository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    private BankAccountService service;

    @BeforeEach
    void setUp() {
        service = new BankAccountServiceImpl(repository, accountNumberGenerator);
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
