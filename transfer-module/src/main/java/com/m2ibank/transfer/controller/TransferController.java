package com.m2ibank.transfer.controller;

import com.m2ibank.common.api.ApiResponse;
import com.m2ibank.transfer.dto.TransferRequestDto;
import com.m2ibank.transfer.dto.TransferResponseDto;
import com.m2ibank.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * REST controller for money transfer endpoints.
 *
 * <p>This class exposes transfer execution, transfer lookup, and account transaction history through
 * {@code /api/v1/transfers}. It keeps HTTP response creation at the edge of the application and delegates
 * money movement rules to {@link TransferService}.</p>
 *
 * <p>The create endpoint uses request-body validation before calling the service. That helps reject
 * missing account numbers, missing amounts, non-positive amounts, and overlong descriptions early.</p>
 */
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "Execute transfers and read transaction history.")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(
            summary = "Execute a transfer",
            description = "Moves money from one account to another after validating the request, checking account state, and enforcing sufficient balance."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Transfer accepted and recorded successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed, accounts are the same, or balance is insufficient"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Source or target account was not found")
    })
    public ResponseEntity<ApiResponse<TransferResponseDto>> executeTransfer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Transfer execution details.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransferRequestDto.class)))
            @Valid @RequestBody TransferRequestDto request) {
        TransferResponseDto transfer = transferService.executeTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(transfer, "Transfer completed successfully"));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get transfer by id",
            description = "Returns one transfer audit record using its internal identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transfer found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Transfer id was not found")
    })
    public ApiResponse<TransferResponseDto> getTransferById(
            @Parameter(description = "Positive internal transfer id.", example = "1")
            @PathVariable Long id) {
        return ApiResponse.success(transferService.getTransferById(id));
    }

    @GetMapping("/account/{accountNumber}")
    @Operation(
            summary = "List account transactions",
            description = "Returns the transfer history where the account was either the source or the target."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Transaction history returned",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    public ApiResponse<List<TransferResponseDto>> getAccountTransactionHistory(
            @Parameter(description = "Generated 12-digit account number.", example = "100000000001")
            @PathVariable String accountNumber) {
        return ApiResponse.success(transferService.getAccountTransactionHistory(accountNumber));
    }
}
