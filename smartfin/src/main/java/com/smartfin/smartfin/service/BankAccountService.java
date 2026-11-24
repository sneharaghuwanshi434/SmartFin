package com.smartfin.smartfin.service;

import com.smartfin.smartfin.entity.BankAccount;
import com.smartfin.smartfin.entity.User;
import com.smartfin.smartfin.exception.ResourceNotFoundException;
import com.smartfin.smartfin.repository.BankAccountRepository;
import com.smartfin.smartfin.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public BankAccountService(
            BankAccountRepository bankAccountRepository,
            UserRepository userRepository) {

        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }
    private String generateAccountNumber() {

        String accountNumber;

        do {
            long number = 100000000000L
                    + secureRandom.nextLong(900000000000L);

            accountNumber = String.valueOf(number);

        } while (bankAccountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
    public BankAccount createAccount(String email, String accountType) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        String normalizedType = accountType.trim().toUpperCase();

        if (!normalizedType.equals("SAVINGS")
                && !normalizedType.equals("CURRENT")) {
            throw new IllegalArgumentException(
                    "Account type must be SAVINGS or CURRENT"
            );
        }

        BankAccount account = new BankAccount();

        account.setAccountNumber(generateAccountNumber());
        account.setUser(user);
        account.setAccountType(normalizedType);
        account.setBalance(BigDecimal.ZERO);

        return bankAccountRepository.save(account);
    }
    public List<BankAccount> getUserAccounts(String email) {

        return bankAccountRepository.findAllByUser_Email(email);
    }
    public BankAccount getAccountByNumber(
            String accountNumber,
            String email) {

        return bankAccountRepository
                .findByAccountNumberAndUser_Email(accountNumber, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account not found"));
    }
}