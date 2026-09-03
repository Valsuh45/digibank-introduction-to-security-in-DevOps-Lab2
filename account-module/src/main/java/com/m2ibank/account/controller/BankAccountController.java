package com.m2ibank.account.controller;

import com.m2ibank.account.dto.AccountRequestDto;
import com.m2ibank.account.dto.AccountResponseDto;
import com.m2ibank.account.service.BankAccountService;
import com.m2ibank.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Accounts", description = "Open bank accounts and retrieve account details.")
public class BankAccountController {

    private final BankAccountService service;

    public BankAccountController(BankAccountService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Open a bank account",
            description = "Creates a current or savings account for an existing customer and assigns a secure generated account number."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer id was not found")
    })
    public ResponseEntity<ApiResponse<AccountResponseDto>> createAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Account opening details.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AccountRequestDto.class)))
            @Valid @RequestBody AccountRequestDto request
    ) {
        AccountResponseDto account = service.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(account, "Account created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get account by id",
            description = "Returns one bank account using its internal identifier."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account id was not found")
    })
    public ResponseEntity<ApiResponse<AccountResponseDto>> getAccountDetails(
            @Parameter(description = "Positive internal account id.", example = "1")
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getAccountDetails(id)));
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(
            summary = "Get account by number",
            description = "Returns one bank account using its generated 12-digit account number."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Account found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Account number was not found")
    })
    public ResponseEntity<ApiResponse<AccountResponseDto>> findByAccountNumber(
            @Parameter(description = "Generated 12-digit account number.", example = "100000000001")
            @PathVariable @Pattern(regexp = "[1-9][0-9]{11}") String accountNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.findByAccountNumber(accountNumber)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(
            summary = "List customer accounts",
            description = "Returns every account owned by one customer."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Customer account list returned",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Customer id was not found")
    })
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getCustomerAccounts(
            @Parameter(description = "Positive internal customer id.", example = "1")
            @PathVariable @Positive Long customerId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getAccountsByCustomerId(customerId)));
    }
}
