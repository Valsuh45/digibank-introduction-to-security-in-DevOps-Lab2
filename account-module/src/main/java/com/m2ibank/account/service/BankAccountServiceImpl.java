package com.m2ibank.account.service;

import com.m2ibank.account.dto.AccountRequestDto;
import com.m2ibank.account.dto.AccountResponseDto;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.entity.BankAccount;
import com.m2ibank.account.repository.BankAccountRepository;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BankAccountServiceImpl implements BankAccountService {

    private static final String DEFAULT_CURRENCY = "XAF";
    private static final int MAX_ACCOUNT_NUMBER_ATTEMPTS = 10;

    private final BankAccountRepository repository;
    private final AccountNumberGenerator accountNumberGenerator;

    public BankAccountServiceImpl(
            BankAccountRepository repository,
            AccountNumberGenerator accountNumberGenerator
    ) {
        this.repository = repository;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    @Override
    @Transactional
    public AccountResponseDto createAccount(AccountRequestDto request) {
        validateRequest(request);

        BankAccount account = BankAccount.open(
                nextAvailableAccountNumber(),
                request.initialBalance(),
                DEFAULT_CURRENCY,
                request.accountType(),
                request.customerId()
        );
        return mapToResponse(repository.save(account));
    }

    @Override
    public AccountResponseDto getAccountDetails(Long id) {
        return mapToResponse(findAccount(id));
    }

    @Override
    public BigDecimal getBalance(Long id) {
        return findAccount(id).getBalance();
    }

    @Override
    public AccountResponseDto findByAccountNumber(String accountNumber) {
        if (accountNumber == null || !accountNumber.matches("[1-9][0-9]{11}")) {
            throw new ResourceNotFoundException("Bank account not found");
        }
        return repository.findByAccountNumber(accountNumber)
                .map(this::mapToResponse)
                .orElseThrow(this::notFound);
    }

    @Override
    public List<AccountResponseDto> getAccountsByCustomerId(Long customerId) {
        if (customerId == null || customerId <= 0) {
            throw new BusinessException("Customer ID must be positive");
        }
        return repository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void updateBalance(String accountNumber, BigDecimal balance) {
        BankAccount account = findAccount(accountNumber);
        account.setBalance(balance);
        repository.save(account);
    }

    private BankAccount findAccount(Long id) {
        if (id == null || id <= 0) {
            throw notFound();
        }
        return repository.findById(id).orElseThrow(this::notFound);
    }

    private BankAccount findAccount(String accountNumber) {
        if (accountNumber == null || !accountNumber.matches("[1-9][0-9]{11}")) {
            throw notFound();
        }
        return repository.findByAccountNumber(accountNumber).orElseThrow(this::notFound);
    }

    private String nextAvailableAccountNumber() {
        for (int attempt = 0; attempt < MAX_ACCOUNT_NUMBER_ATTEMPTS; attempt++) {
            String candidate = accountNumberGenerator.generate();
            if (repository.findByAccountNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new BusinessException("Unable to create bank account");
    }

    private void validateRequest(AccountRequestDto request) {
        if (request == null
                || request.customerId() == null
                || request.customerId() <= 0
                || request.accountType() == null
                || request.initialBalance() == null) {
            throw new BusinessException("Invalid account request");
        }
        if (request.initialBalance().signum() < 0) {
            throw new BusinessException("Initial balance must not be negative");
        }
    }

    private AccountResponseDto mapToResponse(BankAccount account) {
        return new AccountResponseDto(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getAccountType(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getCustomerId()
        );
    }

    private ResourceNotFoundException notFound() {
        return new ResourceNotFoundException("Bank account not found");
    }
}
