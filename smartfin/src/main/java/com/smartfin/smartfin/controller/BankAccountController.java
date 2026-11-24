package com.smartfin.smartfin.controller;

import com.smartfin.smartfin.dto.CreateAccountRequest;
import com.smartfin.smartfin.entity.BankAccount;
import com.smartfin.smartfin.service.BankAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }
    @PostMapping
    public ResponseEntity<?> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        BankAccount account = bankAccountService.createAccount(
                email,
                request.getAccountType()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Bank account created successfully",
                        "accountNumber", account.getAccountNumber(),
                        "accountType", account.getAccountType(),
                        "balance", account.getBalance(),
                        "currency", account.getCurrency(),
                        "status", account.getStatus()
                )
        );
    }
    @GetMapping
    public ResponseEntity<?> getMyAccounts() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<BankAccount> accounts =
                bankAccountService.getUserAccounts(email);

        return ResponseEntity.ok(accounts.stream()
                .map(account -> Map.of(
                        "accountNumber", account.getAccountNumber(),
                        "accountType", account.getAccountType(),
                        "balance", account.getBalance(),
                        "currency", account.getCurrency(),
                        "status", account.getStatus()
                ))
                .toList());
    }
    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getAccountDetails(
            @PathVariable String accountNumber) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        BankAccount account = bankAccountService.getAccountByNumber(
                accountNumber,
                email
        );

        return ResponseEntity.ok(
                Map.of(
                        "accountNumber", account.getAccountNumber(),
                        "accountType", account.getAccountType(),
                        "balance", account.getBalance(),
                        "currency", account.getCurrency(),
                        "status", account.getStatus(),
                        "createdAt", account.getCreatedAt()
                )
        );
    }
}