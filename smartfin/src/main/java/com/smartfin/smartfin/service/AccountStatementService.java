
package com.smartfin.smartfin.service;

import com.smartfin.smartfin.dto.AccountStatementResponse;
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
public class AccountStatementService {

    private final BankAccountRepository bankAccountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public AccountStatementService(
            BankAccountRepository bankAccountRepository,
            LedgerEntryRepository ledgerEntryRepository) {

        this.bankAccountRepository = bankAccountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }


    @Transactional(readOnly = true)
    public List<AccountStatementResponse> getAccountStatement(
            String accountNumber, String email) {

        BankAccount account = bankAccountRepository
                .findByAccountNumberAndUser_Email(accountNumber, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));
        List<LedgerEntry> entries =
                ledgerEntryRepository
                        .findAllByAccount_IdOrderByCreatedAtDesc(
                                account.getId());

        // Calculate opening balance from current balance
        BigDecimal runningBalance = account.getBalance();

        List<AccountStatementResponse> statement =
                new java.util.ArrayList<>();

        for (LedgerEntry entry : entries) {

            if ("CREDIT".equals(entry.getEntryType())) {
                runningBalance = runningBalance.subtract(entry.getAmount());
            } else if ("DEBIT".equals(entry.getEntryType())) {
                runningBalance = runningBalance.add(entry.getAmount());
            }

            statement.add(new AccountStatementResponse(
                    entry.getCreatedAt(),
                    entry.getTransaction().getReferenceNumber(),
                    entry.getTransaction().getTransactionType(),
                    entry.getEntryType(),
                    entry.getAmount(),
                    runningBalance
            ));
        }

        return statement;
    }


}
