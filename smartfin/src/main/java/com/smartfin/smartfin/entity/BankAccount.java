
package com.smartfin.smartfin.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;


    @Column(
            name = "opening_balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal openingBalance;


    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public BankAccount() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }

        if (balance == null) {
            balance = BigDecimal.ZERO;
        }

        if (currency == null) {
            currency = "INR";
        }

        if (status == null) {
            status = "ACTIVE";
        }
        if (openingBalance == null) {
            openingBalance = balance;
        }
    }


    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public UUID getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
    public void withdraw(BigDecimal amount) {

        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient account balance"
            );
        }

        this.balance = this.balance.subtract(amount);
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

