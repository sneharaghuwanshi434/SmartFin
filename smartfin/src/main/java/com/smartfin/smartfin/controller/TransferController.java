
package com.smartfin.smartfin.controller;

import com.smartfin.smartfin.dto.TransferRequest;
import com.smartfin.smartfin.entity.Transaction;
import com.smartfin.smartfin.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(
            @Valid @RequestBody TransferRequest request) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        Transaction transaction =
                transferService.transfer(email, request);

        return ResponseEntity.ok(Map.of(
                "message", "Transfer successful",
                "referenceNumber", transaction.getReferenceNumber(),
                "transactionType", transaction.getTransactionType(),
                "status", transaction.getStatus()
        ));
    }
}
