package com.m2ibank.account.service;

import com.m2ibank.account.dto.AccountRequestDto;
import com.m2ibank.account.dto.AccountResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountService {

    AccountResponseDto createAccount(AccountRequestDto request);

    AccountResponseDto getAccountDetails(Long id);

    BigDecimal getBalance(Long id);

    AccountResponseDto findByAccountNumber(String accountNumber);

    List<AccountResponseDto> getAccountsByCustomerId(Long customerId);

    void updateBalance(String accountNumber, BigDecimal balance);
}
