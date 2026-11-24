package com.smartfin.smartfin.service;

import com.smartfin.smartfin.entity.BankAccount;
import com.smartfin.smartfin.entity.Transaction;
import com.smartfin.smartfin.repository.BankAccountRepository;
import com.smartfin.smartfin.repository.TransactionRepository;
import com.smartfin.smartfin.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.smartfin.smartfin.entity.User;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class WithdrawServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private WithdrawService withdrawService;

    @Test
    void withdrawShouldRejectZeroAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> withdrawService.withdraw(
                        "570929879993",
                        "test@gmail.com",
                        BigDecimal.ZERO,
                        "WDR-TEST-001"
                )
        );
    }

    @Test
    void withdrawShouldRejectNegativeAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> withdrawService.withdraw(
                        "570929879993",
                        "test@gmail.com",
                        new BigDecimal("-100"),
                        "WDR-TEST-002"
                )
        );
    }

    @Test
    void withdrawShouldRejectDuplicateTransaction() {

        String key = "WDR-DUPLICATE-001";

        org.mockito.Mockito.when(
                transactionRepository.findByIdempotencyKey(key)
        ).thenReturn(java.util.Optional.of(new Transaction()));

        assertThrows(
                com.smartfin.smartfin.exception.DuplicateTransactionException.class,
                () -> withdrawService.withdraw(
                        "570929879993",
                        "test@gmail.com",
                        new BigDecimal("100"),
                        key
                )
        );
    }

    @Test
    void withdrawShouldRejectInsufficientBalance() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("100"));

        org.mockito.Mockito.when(
                transactionRepository.findByIdempotencyKey("WDR-INSUFFICIENT-001")
        ).thenReturn(java.util.Optional.empty());

        org.mockito.Mockito.when(
                bankAccountRepository.findByAccountNumberAndUserEmailForUpdate(
                        "570929879993",
                        "test@gmail.com"
                )
        ).thenReturn(java.util.Optional.of(account));

        assertThrows(
                IllegalArgumentException.class,
                () -> withdrawService.withdraw(
                        "570929879993",
                        "test@gmail.com",
                        new BigDecimal("500"),
                        "WDR-INSUFFICIENT-001"
                )
        );
    }

    @Test
    void withdrawShouldSaveSuccessfulTransaction() {

        String key = "WDR-SUCCESS-001";

        BigDecimal amount = new BigDecimal("300");

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("1000"));

        org.mockito.Mockito.when(
                transactionRepository.findByIdempotencyKey(key)
        ).thenReturn(java.util.Optional.empty());

        org.mockito.Mockito.when(
                bankAccountRepository.findByAccountNumberAndUserEmailForUpdate(
                        "570929879993",
                        "test@gmail.com"
                )
        ).thenReturn(java.util.Optional.of(account));

        User user = new User();

        org.mockito.Mockito.when(
                userRepository.findByEmail("test@gmail.com")
        ).thenReturn(java.util.Optional.of(user));

        org.mockito.Mockito.when(
                transactionRepository.save(
                        org.mockito.ArgumentMatchers.any(Transaction.class)
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        withdrawService.withdraw(
                "570929879993",
                "test@gmail.com",
                amount,
                key
        );

        // Verify remaining balance
        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                new BigDecimal("700").compareTo(account.getBalance())
        );

        // Verify account saved
        org.mockito.Mockito.verify(bankAccountRepository).save(account);

        // Verify transaction saved
        org.mockito.Mockito.verify(transactionRepository).save(
                org.mockito.ArgumentMatchers.any(Transaction.class)
        );

        // Verify DEBIT ledger entry
        org.mockito.Mockito.verify(ledgerService).createEntry(
                org.mockito.ArgumentMatchers.any(Transaction.class),
                org.mockito.ArgumentMatchers.eq(account),
                org.mockito.ArgumentMatchers.eq("DEBIT"),
                org.mockito.ArgumentMatchers.eq(amount)
        );
    }
}