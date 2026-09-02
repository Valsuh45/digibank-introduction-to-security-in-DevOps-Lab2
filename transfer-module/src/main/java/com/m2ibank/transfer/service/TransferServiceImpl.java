package com.m2ibank.transfer.service;

import com.m2ibank.account.entity.BankAccount;
import com.m2ibank.account.repository.BankAccountRepository;
import com.m2ibank.common.exception.InsufficientBalanceException;
import com.m2ibank.common.exception.InvalidOperationException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.transfer.dto.TransferRequestDto;
import com.m2ibank.transfer.dto.TransferResponseDto;
import com.m2ibank.transfer.entity.Transfer;
import com.m2ibank.transfer.repository.TransferRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private final BankAccountRepository accountRepository;
    private final TransferRepository transferRepository;

    @Override
    @Transactional
    public TransferResponseDto executeTransfer(TransferRequestDto request) {
        validateRequest(request);

        String sourceAccountNumber = request.sourceAccountNumber().trim();
        String targetAccountNumber = request.targetAccountNumber().trim();
        if (sourceAccountNumber.equals(targetAccountNumber)) {
            throw new InvalidOperationException("Source and target accounts must be different");
        }

        BankAccount sourceAccount = findAccount(sourceAccountNumber);
        BankAccount targetAccount = findAccount(targetAccountNumber);

        if (sourceAccount.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.amount()));
        targetAccount.setBalance(targetAccount.getBalance().add(request.amount()));
        accountRepository.saveAll(List.of(sourceAccount, targetAccount));

        Transfer transfer = Transfer.success(
                generateReference(),
                sourceAccountNumber,
                targetAccountNumber,
                request.amount(),
                Instant.now(),
                normalizeDescription(request.description()));
        Transfer savedTransfer = transferRepository.save(transfer);
        log.info("Transfer {} completed successfully", savedTransfer.getTransferReference());
        return toResponse(savedTransfer);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponseDto getTransferById(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        return toResponse(transfer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferResponseDto> getAccountTransactionHistory(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new InvalidOperationException("Account number is required");
        }

        String normalizedAccountNumber = accountNumber.trim();
        findAccount(normalizedAccountNumber);
        return transferRepository
                .findBySourceAccountNumberOrTargetAccountNumber(
                        normalizedAccountNumber,
                        normalizedAccountNumber)
                .stream()
                .sorted(Comparator.comparing(Transfer::getExecutionDate).reversed())
                .map(this::toResponse)
                .toList();
    }

    private void validateRequest(TransferRequestDto request) {
        if (request == null) {
            throw new InvalidOperationException("Transfer request is required");
        }
        if (request.sourceAccountNumber() == null || request.sourceAccountNumber().isBlank()
                || request.targetAccountNumber() == null || request.targetAccountNumber().isBlank()) {
            throw new InvalidOperationException("Source and target account numbers are required");
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Transfer amount must be greater than zero");
        }
        if (request.description() != null && request.description().length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidOperationException("Transfer description is too long");
        }
    }

    private String generateReference() {
        return "TRF-" + UUID.randomUUID().toString().toUpperCase();
    }

    private BankAccount findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found"));
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private TransferResponseDto toResponse(Transfer transfer) {
        return new TransferResponseDto(
                transfer.getId(),
                transfer.getTransferReference(),
                transfer.getSourceAccountNumber(),
                transfer.getTargetAccountNumber(),
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getExecutionDate(),
                transfer.getDescription());
    }
}
