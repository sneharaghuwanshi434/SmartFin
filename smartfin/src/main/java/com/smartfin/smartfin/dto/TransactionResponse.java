package com.smartfin.smartfin.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TransactionResponse {

    private String referenceNumber;
    private String transactionType;
    private String status;
    private String description;
    private BigDecimal amount;
    private OffsetDateTime createdAt;

    public TransactionResponse(
            String referenceNumber,
            String transactionType,
            String status,
            String description,
            BigDecimal amount,
            OffsetDateTime createdAt) {

        this.referenceNumber = referenceNumber;
        this.transactionType = transactionType;
        this.status = status;
        this.description = description;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
