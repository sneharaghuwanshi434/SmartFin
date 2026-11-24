package com.smartfin.smartfin.repository;

import com.smartfin.smartfin.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findAllByTransaction_Id(UUID transactionId);

    // Fetch ledger entries for multiple transactions in one query

    List<LedgerEntry> findAllByTransaction_IdIn(
            Collection<UUID> transactionIds
    );

    List<LedgerEntry> findAllByAccount_IdOrderByCreatedAtDesc(
            UUID accountId
    );

    Optional<LedgerEntry> findFirstByAccount_IdOrderByCreatedAtAsc(
            UUID accountId
    );
}