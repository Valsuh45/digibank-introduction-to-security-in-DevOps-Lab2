package com.m2ibank.account.controller;

import com.m2ibank.account.dto.AccountRequestDto;
import com.m2ibank.account.dto.AccountResponseDto;
import com.m2ibank.account.service.BankAccountService;
import com.m2ibank.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for account-related API endpoints.
 *
 * <p>This class is the HTTP entry point for creating accounts, reading one account, looking up an
 * account by its generated account number, and listing all accounts owned by one customer. It keeps HTTP
 * status codes and response envelopes close to the web layer while leaving business decisions to
 * {@link BankAccountService}.</p>
 *
 * <p>Validation annotations protect the service layer from malformed path variables and request bodies.
 * The global exception handler turns validation and domain failures into consistent JSON responses.</p>
 */
@Validated
@RestController
@RequestMapping("/api/v1/accounts")
public class BankAccountController {

    private final BankAccountService service;

    public BankAccountController(BankAccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponseDto>> createAccount(
            @Valid @RequestBody AccountRequestDto request
    ) {
        AccountResponseDto account = service.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(account, "Account created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponseDto>> getAccountDetails(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getAccountDetails(id)));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponseDto>> findByAccountNumber(
            @PathVariable @Pattern(regexp = "[1-9][0-9]{11}") String accountNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.findByAccountNumber(accountNumber)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getCustomerAccounts(
            @PathVariable @Positive Long customerId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getAccountsByCustomerId(customerId)));
    }
}
