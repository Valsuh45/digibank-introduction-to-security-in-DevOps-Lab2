package com.m2ibank.transfer.service;

import com.m2ibank.transfer.dto.TransferRequestDto;
import com.m2ibank.transfer.dto.TransferResponseDto;
import java.util.List;

public interface TransferService {

    TransferResponseDto executeTransfer(TransferRequestDto request);

    TransferResponseDto getTransferById(Long id);

    List<TransferResponseDto> getAccountTransactionHistory(String accountNumber);
}
