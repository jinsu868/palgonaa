package com.palgona.palgona.transaction.domain.repository;

import com.palgona.palgona.transaction.domain.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransactionRepository {

    private final TransactionJpaRepository transactionJpaRepository;


    public Transaction save(Transaction transaction) {
        return transactionJpaRepository.save(transaction);
    }
}
