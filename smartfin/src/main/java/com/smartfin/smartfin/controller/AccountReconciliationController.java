
package com.smartfin.smartfin.controller;

import com.smartfin.smartfin.dto.ReconciliationResponse;
import com.smartfin.smartfin.service.AccountReconciliationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountReconciliationController {

    private final AccountReconciliationService reconciliationService;

    public AccountReconciliationController(
            AccountReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @GetMapping("/{accountNumber}/reconciliation")
    public ReconciliationResponse reconcileAccount(
            @PathVariable String accountNumber,
            Authentication authentication) {

        String email = authentication.getName();

        return reconciliationService.reconcileAccount(
                accountNumber, email);
    }

}
