
package com.smartfin.smartfin.service;

import com.smartfin.smartfin.entity.BankAccount;
import com.smartfin.smartfin.entity.LedgerEntry;
import com.smartfin.smartfin.entity.Transaction;
import com.smartfin.smartfin.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public void createEntry(
            Transaction transaction,
            BankAccount account,
            String entryType,
            BigDecimal amount) {

        LedgerEntry entry = new LedgerEntry();

        entry.setTransaction(transaction);
        entry.setAccount(account);
        entry.setEntryType(entryType);
        entry.setAmount(amount);

        ledgerEntryRepository.save(entry);
    }
}
