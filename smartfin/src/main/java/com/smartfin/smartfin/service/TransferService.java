
package com.smartfin.smartfin.service;

import com.smartfin.smartfin.dto.TransferRequest;
import com.smartfin.smartfin.entity.BankAccount;
import com.smartfin.smartfin.entity.Transaction;
import com.smartfin.smartfin.exception.DuplicateTransactionException;
import com.smartfin.smartfin.exception.ResourceNotFoundException;
import com.smartfin.smartfin.repository.BankAccountRepository;
import com.smartfin.smartfin.repository.TransactionRepository;
import com.smartfin.smartfin.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TransferService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final LedgerService ledgerService;

    public TransferService(
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
    public Transaction transfer(String email, TransferRequest request) {

        String sourceNumber = request.getSourceAccountNumber();
        String destinationNumber = request.getDestinationAccountNumber();

        if (sourceNumber.equals(destinationNumber)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts cannot be same");
        }

        // Lock both accounts in sorted order
        List<String> accountNumbers = List.of(sourceNumber, destinationNumber)
                .stream()
                .sorted()
                .toList();

        List<BankAccount> accounts =
                bankAccountRepository.findAccountsForTransferUpdate(accountNumbers);

        if (accounts.size() != 2) {
            throw new ResourceNotFoundException(
                    "One or both accounts not found");
        }

        BankAccount sourceAccount = accounts.stream()
                .filter(a -> a.getAccountNumber().equals(sourceNumber))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Source account not found"));

        BankAccount destinationAccount = accounts.stream()
                .filter(a -> a.getAccountNumber().equals(destinationNumber))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Destination account not found"));

        // Verify sender ownership
        if (sourceAccount.getUser() == null ||
                !sourceAccount.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("You are not authorized to transfer from this account");
        }

        // Check account status
        if (!"ACTIVE".equals(sourceAccount.getStatus()) ||
                !"ACTIVE".equals(destinationAccount.getStatus())) {
            throw new IllegalArgumentException("Both accounts must be active");
        }

        // Check currency
        if (!sourceAccount.getCurrency().equals(destinationAccount.getCurrency())) {
            throw new IllegalArgumentException("Account currencies must match");
        }

        // Check amount
        // Validate idempotency key
        String idempotencyKey = request.getIdempotencyKey();

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required");
        }

// Check duplicate transaction
        Transaction existingTransaction = transactionRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElse(null);

        if (existingTransaction != null) {
            throw new DuplicateTransactionException(
                    "Duplicate transaction request");
        }

        // Debit sender and credit receiver
        sourceAccount.withdraw(request.getAmount());
        destinationAccount.deposit(request.getAmount());

        // Get sender user ID
        UUID userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setReferenceNumber("TRF-" + UUID.randomUUID());
        transaction.setInitiatedBy(userId);
        transaction.setTransactionType("TRANSFER");
        transaction.setStatus("SUCCESS");
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setDescription(
                "Transfer of " + sourceAccount.getCurrency() + " "
                        + request.getAmount() + " from "
                        + sourceNumber + " to " + destinationNumber
        );

        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(destinationAccount);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

// Sender account: DEBIT
        ledgerService.createEntry(
                savedTransaction,
                sourceAccount,
                "DEBIT",
                request.getAmount()
        );

// Receiver account: CREDIT
        ledgerService.createEntry(
                savedTransaction,
                destinationAccount,
                "CREDIT",
                request.getAmount()
        );

        return savedTransaction;
    }
}
