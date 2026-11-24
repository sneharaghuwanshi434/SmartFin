
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
public class DepositService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;

    public DepositService(
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
    public Transaction deposit(
            String accountNumber,
            String email,
            BigDecimal amount,
            String idempotencyKey) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Deposit amount must be greater than zero");
        }

        if (idempotencyKey == null ||
                idempotencyKey.isBlank()) {
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
                        new ResourceNotFoundException("Account not found"));

        Transaction transaction = new Transaction();

        transaction.setReferenceNumber(
                "DEP-" + UUID.randomUUID());

        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"))
                .getId();

        transaction.setInitiatedBy(userId);
        transaction.setTransactionType("DEPOSIT");
        transaction.setStatus("SUCCESS");
        transaction.setIdempotencyKey(idempotencyKey);

        transaction.setDescription("Deposit of INR " + amount);

        account.deposit(amount);

        bankAccountRepository.save(account);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        ledgerService.createEntry(
                savedTransaction,
                account,
                "CREDIT",
                amount
        );

        return savedTransaction;
    }
}
