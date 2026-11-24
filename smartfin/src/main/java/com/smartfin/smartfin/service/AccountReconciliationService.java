
package com.smartfin.smartfin.service;

import com.smartfin.smartfin.dto.ReconciliationResponse;
import com.smartfin.smartfin.entity.BankAccount;
import com.smartfin.smartfin.entity.LedgerEntry;
import com.smartfin.smartfin.exception.ResourceNotFoundException;
import com.smartfin.smartfin.repository.BankAccountRepository;
import com.smartfin.smartfin.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountReconciliationService {

    private final BankAccountRepository bankAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public AccountReconciliationService(
            BankAccountRepository bankAccountRepository,
            LedgerEntryRepository ledgerEntryRepository) {

        this.bankAccountRepository = bankAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public ReconciliationResponse reconcileAccount(
            String accountNumber, String email) {

        BankAccount account = bankAccountRepository
                .findByAccountNumberAndUser_Email(accountNumber, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));

        List<LedgerEntry> entries =
                ledgerEntryRepository
                        .findAllByAccount_IdOrderByCreatedAtDesc(
                                account.getId());

        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;

        for (LedgerEntry entry : entries) {

            if ("CREDIT".equals(entry.getEntryType())) {
                totalCredits = totalCredits.add(entry.getAmount());

            } else if ("DEBIT".equals(entry.getEntryType())) {
                totalDebits = totalDebits.add(entry.getAmount());
            }
        }

        BigDecimal currentBalance = account.getBalance();

        // Read independently stored opening balance
        BigDecimal openingBalance = account.getOpeningBalance();

        // Calculate balance using ledger entries
        BigDecimal calculatedBalance = openingBalance
                .add(totalCredits)
                .subtract(totalDebits);

        // Compare calculated balance with actual account balance
        boolean reconciled =
                calculatedBalance.compareTo(currentBalance) == 0;

        return new ReconciliationResponse(
                account.getAccountNumber(),
                currentBalance,
                openingBalance,
                totalCredits,
                totalDebits,
                calculatedBalance,
                reconciled
        );
    }
}
