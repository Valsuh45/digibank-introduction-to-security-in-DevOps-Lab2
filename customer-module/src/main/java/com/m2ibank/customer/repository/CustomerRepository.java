package com.m2ibank.customer.repository;

import com.m2ibank.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data repository for customer records.
 *
 * <p>The service uses this interface for standard persistence plus exact lookups by normalized email and
 * identity number. Returning {@link Optional} makes missing customers explicit and avoids null checks
 * leaking into controller code.</p>
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByIdentityNumber(String identityNumber);
}
