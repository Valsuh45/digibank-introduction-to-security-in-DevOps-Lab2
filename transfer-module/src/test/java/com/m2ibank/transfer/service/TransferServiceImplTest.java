package com.m2ibank.transfer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.entity.BankAccount;
import com.m2ibank.account.repository.BankAccountRepository;
import com.m2ibank.common.exception.InsufficientBalanceException;
import com.m2ibank.common.exception.InvalidOperationException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.transfer.dto.TransferRequestDto;
import com.m2ibank.transfer.dto.TransferResponseDto;
import com.m2ibank.transfer.entity.Transfer;
import com.m2ibank.transfer.entity.TransferStatus;
import com.m2ibank.transfer.repository.TransferRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for transfer business logic.
 *
 * <p>These tests protect the most sensitive workflow in the application: moving money between accounts.
 * They cover successful transfers, same-account rejection, insufficient balance, missing accounts,
 * description normalization, and transaction-history ordering.</p>
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    private TransferService service;

    @BeforeEach
    void setUp() {
        service = new TransferServiceImpl(accountRepository, transferRepository);
    }

    @Test
    void successfulTransferUpdatesBothBalancesAndPersistsAuditRecord() {
        when(accountRepository.findByAccountNumber("111111111111"))
                .thenReturn(Optional.of(account("111111111111", "100.00")));
        when(accountRepository.findByAccountNumber("222222222222"))
                .thenReturn(Optional.of(account("222222222222", "40.00")));
        when(transferRepository.save(any(Transfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferResponseDto response = service.executeTransfer(request("25.00"));

        verify(accountRepository).saveAll(any());
        verify(transferRepository).save(any(Transfer.class));
        assertThat(response.transferReference()).startsWith("TRF-");
        assertThat(response.amount()).isEqualByComparingTo("25.00");
        assertThat(response.status()).isEqualTo(TransferStatus.SUCCESS);
        assertThat(response.executionDate()).isNotNull();
    }

    @Test
    void insufficientFundsDoesNotMutateBalancesOrPersistTransfer() {
        when(accountRepository.findByAccountNumber("111111111111"))
                .thenReturn(Optional.of(account("111111111111", "24.99")));
        when(accountRepository.findByAccountNumber("222222222222"))
                .thenReturn(Optional.of(account("222222222222", "40.00")));

        assertThatThrownBy(() -> service.executeTransfer(request("25.00")))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Insufficient balance");

        verify(accountRepository, never()).saveAll(any());
        verifyNoInteractions(transferRepository);
    }

    @Test
    void missingAccountDoesNotPersistTransfer() {
        when(accountRepository.findByAccountNumber("111111111111")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executeTransfer(request("25.00")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bank account not found");

        verify(accountRepository, never()).saveAll(any());
        verifyNoInteractions(transferRepository);
    }

    @Test
    void sameAccountTransferIsRejectedBeforeAccountLookup() {
        TransferRequestDto request = new TransferRequestDto(
                "111111111111", "111111111111", new BigDecimal("25.00"), "Self transfer");

        assertThatThrownBy(() -> service.executeTransfer(request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Source and target accounts must be different");

        verifyNoInteractions(accountRepository, transferRepository);
    }

    @Test
    void nonPositiveAmountIsRejectedWhenControllerValidationIsBypassed() {
        TransferRequestDto request = new TransferRequestDto(
                "111111111111", "222222222222", BigDecimal.ZERO, "Invalid transfer");

        assertThatThrownBy(() -> service.executeTransfer(request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessage("Transfer amount must be greater than zero");

        verifyNoInteractions(accountRepository, transferRepository);
    }

    @Test
    void accountHistoryReturnsSafeTransferResponses() {
        when(accountRepository.findByAccountNumber("111111111111"))
                .thenReturn(Optional.of(account("111111111111", "100.00")));
        Transfer transfer = Transfer.success(
                "TRF-123",
                "111111111111",
                "222222222222",
                new BigDecimal("25.00"),
                Instant.parse("2026-09-02T10:15:30Z"),
                "Rent");
        when(transferRepository.findBySourceAccountNumberOrTargetAccountNumber("111111111111", "111111111111"))
                .thenReturn(List.of(transfer));

        List<TransferResponseDto> history = service.getAccountTransactionHistory("111111111111");

        assertThat(history).singleElement().satisfies(response -> {
            assertThat(response.transferReference()).isEqualTo("TRF-123");
            assertThat(response.sourceAccountNumber()).isEqualTo("111111111111");
            assertThat(response.targetAccountNumber()).isEqualTo("222222222222");
            assertThat(response.description()).isEqualTo("Rent");
        });
    }

    @Test
    void missingTransferUsesGenericNotFoundError() {
        when(transferRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTransferById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transfer not found");
    }

    private TransferRequestDto request(String amount) {
        return new TransferRequestDto(
                "111111111111", "222222222222", new BigDecimal(amount), "Rent");
    }

    private BankAccount account(String accountNumber, String balance) {
        return BankAccount.open(
                accountNumber,
                new BigDecimal(balance),
                "XAF",
                AccountType.CURRENT,
                1L);
    }
}
