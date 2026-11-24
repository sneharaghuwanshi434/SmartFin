package com.smartfin.smartfin.service;

import com.smartfin.smartfin.dto.TransactionAnalyticsResponse;
import com.smartfin.smartfin.dto.TransactionResponse;
import com.smartfin.smartfin.entity.LedgerEntry;
import com.smartfin.smartfin.entity.Transaction;
import com.smartfin.smartfin.entity.User;
import com.smartfin.smartfin.repository.LedgerEntryRepository;
import com.smartfin.smartfin.repository.TransactionRepository;
import com.smartfin.smartfin.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            LedgerEntryRepository ledgerEntryRepository) {

        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    // Transaction History

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(
            String email,
            int page,
            int size,
            String transactionType) {

        validatePagination(page, size);

        User user = getUser(email);
        Pageable pageable = createPageable(page, size);
        String type = normalizeTransactionType(transactionType);

        Page<Transaction> transactions;

        if (type == null) {
            transactions =
                    transactionRepository
                            .findAllByInitiatedByOrderByCreatedAtDesc(
                                    user.getId(), pageable);
        } else {
            transactions =
                    transactionRepository
                            .findAllByInitiatedByAndTransactionTypeOrderByCreatedAtDesc(
                                    user.getId(), type, pageable);
        }

        return transactions.map(this::mapToResponse);
    }

    // Transaction History with Date Range

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactionsByDateRange(
            String email,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            int page,
            int size,
            String transactionType) {

        validatePagination(page, size);
        validateDateRange(startDate, endDate);

        User user = getUser(email);
        Pageable pageable = createPageable(page, size);
        String type = normalizeTransactionType(transactionType);

        Page<Transaction> transactions;

        if (type == null) {
            transactions =
                    transactionRepository
                            .findAllByInitiatedByAndCreatedAtBetweenOrderByCreatedAtDesc(
                                    user.getId(),
                                    startDate,
                                    endDate,
                                    pageable);
        } else {
            transactions =
                    transactionRepository
                            .findAllByInitiatedByAndTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
                                    user.getId(),
                                    type,
                                    startDate,
                                    endDate,
                                    pageable);
        }

        return transactions.map(this::mapToResponse);
    }

    // Complete Transaction Analytics (Without Pagination)

    @Transactional(readOnly = true)
    public TransactionAnalyticsResponse getUserTransactionAnalytics(
            String email,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String transactionType) {

        if ((startDate == null) != (endDate == null)) {
            throw new IllegalArgumentException(
                    "Both start date and end date are required");
        }

        if (startDate != null) {
            validateDateRange(startDate, endDate);
        }

        User user = getUser(email);
        String type = normalizeTransactionType(transactionType);

        List<Transaction> transactions;

        if (startDate == null && type == null) {

            transactions =
                    transactionRepository
                            .findAllByInitiatedByOrderByCreatedAtDesc(
                                    user.getId());

        } else if (startDate == null) {

            transactions =
                    transactionRepository
                            .findAllByInitiatedByAndTransactionTypeOrderByCreatedAtDesc(
                                    user.getId(), type);

        } else if (type == null) {

            transactions =
                    transactionRepository
                            .findAllByInitiatedByAndCreatedAtBetweenOrderByCreatedAtDesc(
                                    user.getId(),
                                    startDate,
                                    endDate);

        } else {

            transactions =
                    transactionRepository
                            .findAllByInitiatedByAndTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
                                    user.getId(),
                                    type,
                                    startDate,
                                    endDate);
        }

        return calculateAnalytics(transactions);
    }

    private TransactionAnalyticsResponse calculateAnalytics(
            List<Transaction> transactions) {

        Map<UUID, AmountAccumulator> amountMap = new HashMap<>();

        if (!transactions.isEmpty()) {

            List<UUID> transactionIds = transactions.stream()
                    .map(Transaction::getId)
                    .toList();

            List<LedgerEntry> entries =
                    ledgerEntryRepository.findAllByTransaction_IdIn(
                            transactionIds);

            for (LedgerEntry entry : entries) {

                UUID transactionId = entry.getTransaction().getId();

                AmountAccumulator accumulator =
                        amountMap.computeIfAbsent(
                                transactionId,
                                id -> new AmountAccumulator());

                if ("DEBIT".equalsIgnoreCase(entry.getEntryType())) {
                    accumulator.debit =
                            accumulator.debit.add(entry.getAmount());
                }

                if ("CREDIT".equalsIgnoreCase(entry.getEntryType())) {
                    accumulator.credit =
                            accumulator.credit.add(entry.getAmount());
                }
            }
        }

        Map<String, TypeAccumulator> typeMap =
                new LinkedHashMap<>();

        typeMap.put("DEPOSIT", new TypeAccumulator());
        typeMap.put("WITHDRAWAL", new TypeAccumulator());
        typeMap.put("TRANSFER", new TypeAccumulator());

        Map<LocalDate, DailyAccumulator> dailyMap =
                new TreeMap<>();

        BigDecimal totalDepositAmount = BigDecimal.ZERO;
        BigDecimal totalWithdrawalAmount = BigDecimal.ZERO;
        BigDecimal totalTransferAmount = BigDecimal.ZERO;

        long totalDeposits = 0;
        long totalWithdrawals = 0;
        long totalTransfers = 0;

        for (Transaction transaction : transactions) {

            AmountAccumulator ledgerAmount =
                    amountMap.get(transaction.getId());

            BigDecimal amount = BigDecimal.ZERO;

            if (ledgerAmount != null) {
                amount = ledgerAmount.debit.compareTo(BigDecimal.ZERO) > 0
                        ? ledgerAmount.debit
                        : ledgerAmount.credit;
            }

            String transactionType =
                    transaction.getTransactionType()
                            .toUpperCase(Locale.ROOT);

            TypeAccumulator typeAccumulator =
                    typeMap.computeIfAbsent(
                            transactionType,
                            key -> new TypeAccumulator());

            typeAccumulator.count++;
            typeAccumulator.totalAmount =
                    typeAccumulator.totalAmount.add(amount);

            if ("DEPOSIT".equals(transactionType)) {
                totalDeposits++;
                totalDepositAmount =
                        totalDepositAmount.add(amount);
            }

            if ("WITHDRAWAL".equals(transactionType)) {
                totalWithdrawals++;
                totalWithdrawalAmount =
                        totalWithdrawalAmount.add(amount);
            }

            if ("TRANSFER".equals(transactionType)) {
                totalTransfers++;
                totalTransferAmount =
                        totalTransferAmount.add(amount);
            }

            LocalDate date =
                    transaction.getCreatedAt().toLocalDate();

            DailyAccumulator dailyAccumulator =
                    dailyMap.computeIfAbsent(
                            date,
                            key -> new DailyAccumulator());

            dailyAccumulator.count++;
            dailyAccumulator.totalAmount =
                    dailyAccumulator.totalAmount.add(amount);
        }

        List<TransactionAnalyticsResponse.TypeSummary> typeSummaries =
                new ArrayList<>();

        typeMap.forEach((type, accumulator) ->
                typeSummaries.add(
                        new TransactionAnalyticsResponse.TypeSummary(
                                type,
                                accumulator.count,
                                accumulator.totalAmount
                        )
                )
        );

        List<TransactionAnalyticsResponse.DailyActivity> dailyActivities =
                new ArrayList<>();

        dailyMap.forEach((date, accumulator) ->
                dailyActivities.add(
                        new TransactionAnalyticsResponse.DailyActivity(
                                date,
                                accumulator.count,
                                accumulator.totalAmount
                        )
                )
        );

        return new TransactionAnalyticsResponse(
                transactions.size(),
                totalDeposits,
                totalWithdrawals,
                totalTransfers,
                totalDepositAmount,
                totalWithdrawalAmount,
                totalTransferAmount,
                typeSummaries,
                dailyActivities
        );
    }

    private static class AmountAccumulator {
        private BigDecimal debit = BigDecimal.ZERO;
        private BigDecimal credit = BigDecimal.ZERO;
    }

    private static class TypeAccumulator {
        private long count = 0;
        private BigDecimal totalAmount = BigDecimal.ZERO;
    }

    private static class DailyAccumulator {
        private long count = 0;
        private BigDecimal totalAmount = BigDecimal.ZERO;
    }

    // Secure Transaction Receipt Lookup

    @Transactional(readOnly = true)
    public TransactionResponse getUserTransactionByReference(
            String email,
            String referenceNumber) {

        User user = getUser(email);

        Transaction transaction = transactionRepository
                .findByReferenceNumberAndInitiatedBy(
                        referenceNumber,
                        user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found"));

        return mapToResponse(transaction);
    }

    // Transaction Type Validation

    private String normalizeTransactionType(String transactionType) {

        if (transactionType == null
                || transactionType.isBlank()
                || transactionType.equalsIgnoreCase("ALL")) {
            return null;
        }

        String type =
                transactionType.trim().toUpperCase(Locale.ROOT);

        if (!List.of("DEPOSIT", "WITHDRAWAL", "TRANSFER")
                .contains(type)) {

            throw new IllegalArgumentException(
                    "Invalid transaction type");
        }

        return type;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Invalid pagination parameters");
        }
    }

    private void validateDateRange(
            OffsetDateTime startDate,
            OffsetDateTime endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date");
        }
    }

    private Pageable createPageable(int page, int size) {
        return PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );
    }

    // Existing Transaction Response Mapper

    private TransactionResponse mapToResponse(
            Transaction transaction) {

        List<LedgerEntry> entries =
                ledgerEntryRepository.findAllByTransaction_Id(
                        transaction.getId());

        BigDecimal debitAmount = entries.stream()
                .filter(entry ->
                        "DEBIT".equalsIgnoreCase(entry.getEntryType()))
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditAmount = entries.stream()
                .filter(entry ->
                        "CREDIT".equalsIgnoreCase(entry.getEntryType()))
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal amount =
                debitAmount.compareTo(BigDecimal.ZERO) > 0
                        ? debitAmount
                        : creditAmount;

        return new TransactionResponse(
                transaction.getReferenceNumber(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getDescription(),
                amount,
                transaction.getCreatedAt()
        );
    }
}