package com.palgona.palgona.transaction.domain.repository;

import com.palgona.palgona.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionJpaRepository extends JpaRepository<Transaction, Long> {
}
