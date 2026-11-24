
package com.smartfin.smartfin.controller;

import com.smartfin.smartfin.dto.WithdrawRequest;
import com.smartfin.smartfin.entity.Transaction;
import com.smartfin.smartfin.service.WithdrawService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class WithdrawController {

    private final WithdrawService withdrawService;

    public WithdrawController(WithdrawService withdrawService) {
        this.withdrawService = withdrawService;
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @Valid @RequestBody WithdrawRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Transaction transaction = withdrawService.withdraw(
                request.getAccountNumber(),
                email,
                request.getAmount(),
                request.getIdempotencyKey()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Withdrawal successful",
                        "referenceNumber", transaction.getReferenceNumber(),
                        "transactionType", transaction.getTransactionType(),
                        "status", transaction.getStatus()
                )
        );
    }
}
