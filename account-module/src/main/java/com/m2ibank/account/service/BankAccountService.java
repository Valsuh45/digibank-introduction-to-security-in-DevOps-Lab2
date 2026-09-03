package com.m2ibank.account.service;

import com.m2ibank.account.dto.AccountRequestDto;
import com.m2ibank.account.dto.AccountResponseDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Application service contract for account operations.
 *
 * <p>Controllers and other modules depend on this interface instead of the implementation class. That
 * keeps the web layer small and allows transfer logic to read and update accounts through a stable API.</p>
 *
 * <p>The implementation is responsible for validation that cannot be expressed by request annotations,
 * account-number generation, persistence, and response mapping.</p>
 */
public interface BankAccountService {

    AccountResponseDto createAccount(AccountRequestDto request);

    AccountResponseDto getAccountDetails(Long id);

    BigDecimal getBalance(Long id);

    AccountResponseDto findByAccountNumber(String accountNumber);

    List<AccountResponseDto> getAccountsByCustomerId(Long customerId);

    void updateBalance(String accountNumber, BigDecimal balance);
}
