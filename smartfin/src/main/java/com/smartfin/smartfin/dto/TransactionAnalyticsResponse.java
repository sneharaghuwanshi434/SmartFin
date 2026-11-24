package com.smartfin.smartfin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TransactionAnalyticsResponse(
        long totalTransactions,
        long totalDeposits,
        long totalWithdrawals,
        long totalTransfers,
        BigDecimal totalDepositAmount,
        BigDecimal totalWithdrawalAmount,
        BigDecimal totalTransferAmount,
        List<TypeSummary> transactionTypeBreakdown,
        List<DailyActivity> dailyActivity
) {

    public record TypeSummary(
            String transactionType,
            long count,
            BigDecimal totalAmount
    ) {}

    public record DailyActivity(
            LocalDate date,
            long transactionCount,
            BigDecimal totalAmount
    ) {}
}