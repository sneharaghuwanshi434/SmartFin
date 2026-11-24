
package com.smartfin.smartfin.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class AccountStatementResponse {

    private OffsetDateTime transactionDate;
    private String referenceNumber;
    private String transactionType;
    private String entryType;
    private BigDecimal amount;
    private BigDecimal balance;

    public AccountStatementResponse(
            OffsetDateTime transactionDate,
            String referenceNumber,
            String transactionType,
            String entryType,
            BigDecimal amount,
            BigDecimal balance) {

        this.transactionDate = transactionDate;
        this.referenceNumber = referenceNumber;
        this.transactionType = transactionType;
        this.entryType = entryType;
        this.amount = amount;
        this.balance = balance;
    }

    public OffsetDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getEntryType() {
        return entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }


}
