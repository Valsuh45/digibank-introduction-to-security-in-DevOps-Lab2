package com.m2ibank.transfer.repository;

import com.m2ibank.transfer.entity.Transfer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findBySourceAccountNumberOrTargetAccountNumber(
            String sourceAccountNumber,
            String targetAccountNumber);
}
