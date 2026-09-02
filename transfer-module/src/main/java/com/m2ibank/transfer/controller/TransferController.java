package com.m2ibank.transfer.controller;

import com.m2ibank.common.api.ApiResponse;
import com.m2ibank.transfer.dto.TransferRequestDto;
import com.m2ibank.transfer.dto.TransferResponseDto;
import com.m2ibank.transfer.service.TransferService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransferResponseDto>> executeTransfer(
            @Valid @RequestBody TransferRequestDto request) {
        TransferResponseDto transfer = transferService.executeTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(transfer, "Transfer completed successfully"));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransferResponseDto> getTransferById(@PathVariable Long id) {
        return ApiResponse.success(transferService.getTransferById(id));
    }

    @GetMapping("/account/{accountNumber}")
    public ApiResponse<List<TransferResponseDto>> getAccountTransactionHistory(
            @PathVariable String accountNumber) {
        return ApiResponse.success(transferService.getAccountTransactionHistory(accountNumber));
    }
}
