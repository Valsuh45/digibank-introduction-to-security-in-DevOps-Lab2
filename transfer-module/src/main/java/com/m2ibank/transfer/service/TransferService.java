package com.m2ibank.transfer.service;

import com.m2ibank.transfer.dto.TransferRequestDto;
import com.m2ibank.transfer.dto.TransferResponseDto;
import java.util.List;

/**
 * Application service contract for transfers.
 *
 * <p>This interface defines the operations the web layer can perform: execute a transfer, read one
 * transfer, and read the transaction history for an account. The implementation owns account lookup,
 * balance checks, transaction handling, and audit record creation.</p>
 */
public interface TransferService {

    TransferResponseDto executeTransfer(TransferRequestDto request);

    TransferResponseDto getTransferById(Long id);

    List<TransferResponseDto> getAccountTransactionHistory(String accountNumber);
}
