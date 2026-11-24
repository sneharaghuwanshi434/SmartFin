
package com.smartfin.smartfin.dto;

import java.math.BigDecimal;

public class ReconciliationResponse {

    private String accountNumber;
    private BigDecimal currentBalance;
    private BigDecimal openingBalance;
    private BigDecimal totalCredits;
    private BigDecimal totalDebits;
    private BigDecimal calculatedBalance;
    private boolean reconciled;

    public ReconciliationResponse(
            String accountNumber,
            BigDecimal currentBalance,
            BigDecimal openingBalance,
            BigDecimal totalCredits,
            BigDecimal totalDebits,
            BigDecimal calculatedBalance,
            boolean reconciled) {

        this.accountNumber = accountNumber;
        this.currentBalance = currentBalance;
        this.openingBalance = openingBalance;
        this.totalCredits = totalCredits;
        this.totalDebits = totalDebits;
        this.calculatedBalance = calculatedBalance;
        this.reconciled = reconciled;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public BigDecimal getTotalCredits() {
        return totalCredits;
    }

    public BigDecimal getTotalDebits() {
        return totalDebits;
    }

    public BigDecimal getCalculatedBalance() {
        return calculatedBalance;
    }

    public boolean isReconciled() {
        return reconciled;
    }
}
