
package com.smartfin.smartfin.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transactions_idempotency_key",
                        columnNames = "idempotency_key"
                )
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reference_number", nullable = false, length = 50)
    private String referenceNumber;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "initiated_by")
    private UUID initiatedBy;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Transaction() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(UUID initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
