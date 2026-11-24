
package com.smartfin.smartfin.service;

import com.smartfin.smartfin.entity.BankAccount;
import com.smartfin.smartfin.entity.Transaction;
import com.smartfin.smartfin.exception.DuplicateTransactionException;
import com.smartfin.smartfin.exception.ResourceNotFoundException;
import com.smartfin.smartfin.repository.BankAccountRepository;
import com.smartfin.smartfin.repository.TransactionRepository;
import com.smartfin.smartfin.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WithdrawService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final LedgerService ledgerService;

    public WithdrawService(
            BankAccountRepository bankAccountRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            LedgerService ledgerService) {

        this.bankAccountRepository = bankAccountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public Transaction withdraw(
            String accountNumber,
            String email,
            BigDecimal amount, String idempotencyKey) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount must be greater than zero");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required");
        }

        Transaction existingTransaction = transactionRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElse(null);

        if (existingTransaction != null) {
            throw new DuplicateTransactionException(
                    "Duplicate transaction request");
        }
        BankAccount account = bankAccountRepository
                .findByAccountNumberAndUserEmailForUpdate(
                        accountNumber, email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found"));

        account.withdraw(amount);

        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found")).getId();
        Transaction transaction = new Transaction();
        transaction.setReferenceNumber("WDR-" + UUID.randomUUID());
        transaction.setInitiatedBy(userId);
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setStatus("SUCCESS");
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setDescription("Withdrawal of INR " + amount);

        bankAccountRepository.save(account);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

// Withdrawal means money leaves the account
        ledgerService.createEntry(
                savedTransaction,
                account,
                "DEBIT",
                amount
        );

        return savedTransaction;
    }
}
