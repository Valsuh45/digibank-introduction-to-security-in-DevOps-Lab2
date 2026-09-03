package com.m2ibank.transfer.repository;

import com.m2ibank.transfer.entity.Transfer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for transfer audit records.
 *
 * <p>The custom finder returns every transfer where an account appears as either the source or target.
 * The service sorts that result by execution date before returning transaction history to clients.</p>
 */
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findBySourceAccountNumberOrTargetAccountNumber(
            String sourceAccountNumber,
            String targetAccountNumber);
}
