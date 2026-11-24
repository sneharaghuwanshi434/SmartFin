package com.smartfin.smartfin.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateAccountRequest {

    @NotBlank(message = "Account type is required")
    private String accountType;

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}