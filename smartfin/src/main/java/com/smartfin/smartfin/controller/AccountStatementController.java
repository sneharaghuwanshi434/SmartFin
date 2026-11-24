
package com.smartfin.smartfin.controller;

import com.smartfin.smartfin.dto.AccountStatementResponse;
import com.smartfin.smartfin.service.AccountStatementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountStatementController {

    private final AccountStatementService accountStatementService;

    public AccountStatementController(
            AccountStatementService accountStatementService) {
        this.accountStatementService = accountStatementService;
    }

    @GetMapping("/{accountNumber}/statement")
    public ResponseEntity<List<AccountStatementResponse>> getStatement(
            @PathVariable String accountNumber) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        List<AccountStatementResponse> statement =
                accountStatementService.getAccountStatement(
                        accountNumber, email);

        return ResponseEntity.ok(statement);
    }
}
