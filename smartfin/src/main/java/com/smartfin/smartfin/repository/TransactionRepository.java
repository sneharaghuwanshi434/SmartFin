package com.smartfin.smartfin.repository;

import com.smartfin.smartfin.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {

    // Paginated transaction history

    Page<Transaction> findAllByInitiatedByOrderByCreatedAtDesc(
            UUID initiatedBy,
            Pageable pageable
    );

    Page<Transaction> findAllByInitiatedByAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID initiatedBy,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable
    );

    Page<Transaction> findAllByInitiatedByAndTransactionTypeOrderByCreatedAtDesc(
            UUID initiatedBy,
            String transactionType,
            Pageable pageable
    );

    Page<Transaction> findAllByInitiatedByAndTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID initiatedBy,
            String transactionType,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable
    );

    // Complete transaction data for analytics (without pagination)

    List<Transaction> findAllByInitiatedByOrderByCreatedAtDesc(
            UUID initiatedBy
    );

    List<Transaction> findAllByInitiatedByAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID initiatedBy,
            OffsetDateTime startDate,
            OffsetDateTime endDate
    );

    List<Transaction> findAllByInitiatedByAndTransactionTypeOrderByCreatedAtDesc(
            UUID initiatedBy,
            String transactionType
    );

    List<Transaction> findAllByInitiatedByAndTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            UUID initiatedBy,
            String transactionType,
            OffsetDateTime startDate,
            OffsetDateTime endDate
    );

    // Existing methods

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByReferenceNumberAndInitiatedBy(
            String referenceNumber,
            UUID initiatedBy
    );
}