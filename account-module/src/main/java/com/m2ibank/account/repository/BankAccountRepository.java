package com.m2ibank.account.repository;

import com.m2ibank.account.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for account persistence.
 *
 * <p>The inherited {@link JpaRepository} methods handle standard create, read, update, and delete
 * operations. The custom finder methods support the account service and transfer service without
 * requiring handwritten SQL.</p>
 *
 * <p>Lookups by account number return {@link Optional} so services must handle missing accounts
 * explicitly instead of receiving null values.</p>
 */
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    List<BankAccount> findByCustomerId(Long customerId);
}
