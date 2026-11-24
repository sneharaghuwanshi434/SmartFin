
package com.smartfin.smartfin.controller;

import com.smartfin.smartfin.dto.DepositRequest;
import com.smartfin.smartfin.entity.Transaction;
import com.smartfin.smartfin.service.DepositService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @Valid @RequestBody DepositRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Transaction transaction = depositService.deposit(
                request.getAccountNumber(),
                email,
                request.getAmount(),
                request.getIdempotencyKey()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Deposit successful",
                        "referenceNumber", transaction.getReferenceNumber(),
                        "transactionType", transaction.getTransactionType(),
                        "status", transaction.getStatus()
                )
        );
    }
}

