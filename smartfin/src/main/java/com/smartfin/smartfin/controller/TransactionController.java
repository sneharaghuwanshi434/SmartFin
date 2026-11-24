package com.smartfin.smartfin.controller;

import com.smartfin.smartfin.dto.DepositRequest;
import com.smartfin.smartfin.dto.TransferRequest;
import com.smartfin.smartfin.dto.TransactionResponse;
import com.smartfin.smartfin.dto.TransactionAnalyticsResponse;

import com.smartfin.smartfin.service.DepositService;
import com.smartfin.smartfin.service.TransactionService;
import com.smartfin.smartfin.service.WithdrawService;
import com.smartfin.smartfin.service.TransferService;
import com.smartfin.smartfin.service.ReceiptService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final DepositService depositService;
    private final WithdrawService withdrawService;
    private final TransferService transferService;
    private final ReceiptService receiptService;

    public TransactionController(
            TransactionService transactionService,
            DepositService depositService,
            WithdrawService withdrawService,
            TransferService transferService,
            ReceiptService receiptService) {

        this.transactionService = transactionService;
        this.depositService = depositService;
        this.withdrawService = withdrawService;
        this.transferService = transferService;
        this.receiptService = receiptService;
    }

    // Transaction History

    @GetMapping("/history")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String transactionType) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Page<TransactionResponse> transactions =
                transactionService.getUserTransactions(
                        email,
                        page,
                        size,
                        transactionType
                );

        return ResponseEntity.ok(transactions);
    }

    // Transaction History with Date Range

    @GetMapping("/history/filter")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistoryByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String transactionType) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Page<TransactionResponse> transactions =
                transactionService.getUserTransactionsByDateRange(
                        email,
                        startDate,
                        endDate,
                        page,
                        size,
                        transactionType
                );

        return ResponseEntity.ok(transactions);
    }

    // Complete Transaction Analytics

    @GetMapping("/analytics")
    public ResponseEntity<TransactionAnalyticsResponse> getTransactionAnalytics(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime endDate,

            @RequestParam(required = false)
            String transactionType) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        TransactionAnalyticsResponse analytics =
                transactionService.getUserTransactionAnalytics(
                        email,
                        startDate,
                        endDate,
                        transactionType
                );

        return ResponseEntity.ok(analytics);
    }

    // Download Transaction PDF Receipt

    @GetMapping("/{referenceNumber}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable String referenceNumber) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        TransactionResponse transaction =
                transactionService.getUserTransactionByReference(
                        email,
                        referenceNumber
                );

        byte[] pdf = receiptService.generateReceipt(transaction);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(
                                        "SmartFin-Receipt-" +
                                                referenceNumber + ".pdf"
                                )
                                .build()
                                .toString()
                )
                .body(pdf);
    }

    // Deposit Money

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @Valid @RequestBody DepositRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        var transaction = depositService.deposit(
                request.getAccountNumber(),
                email,
                request.getAmount(),
                request.getIdempotencyKey()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Deposit successful",
                "referenceNumber", transaction.getReferenceNumber(),
                "transactionType", transaction.getTransactionType(),
                "status", transaction.getStatus()
        ));
    }

    // Withdraw Money

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @Valid @RequestBody DepositRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        var transaction = withdrawService.withdraw(
                request.getAccountNumber(),
                email,
                request.getAmount(),
                request.getIdempotencyKey()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Withdrawal successful",
                "referenceNumber", transaction.getReferenceNumber(),
                "transactionType", transaction.getTransactionType(),
                "status", transaction.getStatus()
        ));
    }

    // Transfer Money

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @Valid @RequestBody TransferRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        var transaction = transferService.transfer(email, request);

        return ResponseEntity.ok(Map.of(
                "message", "Transfer successful",
                "referenceNumber", transaction.getReferenceNumber(),
                "transactionType", transaction.getTransactionType(),
                "status", transaction.getStatus()
        ));
    }
}