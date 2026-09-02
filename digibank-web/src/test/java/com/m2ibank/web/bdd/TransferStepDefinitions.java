package com.m2ibank.web.bdd;

import com.m2ibank.account.dto.AccountResponseDto;
import com.m2ibank.account.service.BankAccountService;
import com.m2ibank.common.exception.InsufficientBalanceException;
import com.m2ibank.transfer.dto.TransferRequestDto;
import com.m2ibank.transfer.dto.TransferResponseDto;
import com.m2ibank.transfer.entity.TransferStatus;
import com.m2ibank.transfer.service.TransferService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TransferStepDefinitions {

    @Autowired
    private BankAccountService accountService;

    @Autowired
    private TransferService transferService;

    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal sourceStartingBalance;
    private BigDecimal targetStartingBalance;
    private BigDecimal transferAmount;
    private TransferResponseDto transferResponse;

    @Given("the seeded DigiBank accounts are available")
    public void seededAccountsAreAvailable() {
        AccountResponseDto source = accountService.findByAccountNumber("100000000001");
        AccountResponseDto target = accountService.findByAccountNumber("100000000002");

        assertThat(source.status().name()).isEqualTo("ACTIVE");
        assertThat(target.status().name()).isEqualTo("ACTIVE");
    }

    @When("a transfer of {bigdecimal} XAF is executed from account {string} to account {string}")
    public void transferIsExecuted(BigDecimal amount, String source, String target) {
        sourceAccountNumber = source;
        targetAccountNumber = target;
        transferAmount = amount;
        sourceStartingBalance = accountService.findByAccountNumber(source).balance();
        targetStartingBalance = accountService.findByAccountNumber(target).balance();

        transferResponse = transferService.executeTransfer(
                new TransferRequestDto(source, target, amount, "Cucumber transfer evidence"));
    }

    @When("an excessive transfer is attempted from account {string} to account {string}")
    public void excessiveTransferIsAttempted(String source, String target) {
        sourceAccountNumber = source;
        targetAccountNumber = target;
        sourceStartingBalance = accountService.findByAccountNumber(source).balance();
        transferAmount = sourceStartingBalance.add(BigDecimal.ONE);

        assertThatThrownBy(() -> transferService.executeTransfer(
                new TransferRequestDto(source, target, transferAmount, "Insufficient funds evidence")))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Insufficient balance");
    }

    @Then("the transfer is recorded as successful")
    public void transferIsRecordedAsSuccessful() {
        assertThat(transferResponse).isNotNull();
        assertThat(transferResponse.status()).isEqualTo(TransferStatus.SUCCESS);
        assertThat(transferResponse.transferReference()).startsWith("TRF-");
        assertThat(transferResponse.sourceAccountNumber()).isEqualTo(sourceAccountNumber);
        assertThat(transferResponse.targetAccountNumber()).isEqualTo(targetAccountNumber);
    }

    @And("the source and target balances are updated atomically")
    public void balancesAreUpdatedAtomically() {
        BigDecimal sourceEndingBalance = accountService.findByAccountNumber(sourceAccountNumber).balance();
        BigDecimal targetEndingBalance = accountService.findByAccountNumber(targetAccountNumber).balance();

        assertThat(sourceEndingBalance).isEqualByComparingTo(sourceStartingBalance.subtract(transferAmount));
        assertThat(targetEndingBalance).isEqualByComparingTo(targetStartingBalance.add(transferAmount));
    }

    @Then("the transfer is rejected for insufficient balance")
    public void transferIsRejectedForInsufficientBalance() {
        assertThat(transferAmount).isGreaterThan(sourceStartingBalance);
    }

    @And("the source balance remains unchanged")
    public void sourceBalanceRemainsUnchanged() {
        BigDecimal sourceEndingBalance = accountService.findByAccountNumber(sourceAccountNumber).balance();

        assertThat(sourceEndingBalance).isEqualByComparingTo(sourceStartingBalance);
    }
}
