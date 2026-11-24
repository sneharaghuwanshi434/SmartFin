package com.smartfin.smartfin.service;

import com.smartfin.smartfin.entity.BankAccount;
import com.smartfin.smartfin.entity.Transaction;
import com.smartfin.smartfin.entity.User;
import com.smartfin.smartfin.exception.DuplicateTransactionException;
import com.smartfin.smartfin.repository.BankAccountRepository;
import com.smartfin.smartfin.repository.TransactionRepository;
import com.smartfin.smartfin.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private DepositService depositService;

    @Test
    void depositShouldRejectZeroAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> depositService.deposit(
                        "570929879993",
                        "test@gmail.com",
                        BigDecimal.ZERO,
                        "TEST-KEY-001"
                )
        );
    }
    @Test
    void depositShouldRejectNegativeAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> depositService.deposit(
                        "570929879993",
                        "test@gmail.com",
                        new BigDecimal("-100"),
                        "TEST-KEY-002"
                )
        );
    }

    @Test
    void depositShouldRejectDuplicateTransaction() {

        String key = "DEP-DUPLICATE-001";

        Transaction existingTransaction = new Transaction();

        org.mockito.Mockito.when(
                transactionRepository.findByIdempotencyKey(key)
        ).thenReturn(java.util.Optional.of(existingTransaction));

        assertThrows(
                DuplicateTransactionException.class,
                () -> depositService.deposit(
                        "570929879993",
                        "test@gmail.com",
                        new BigDecimal("100"),
                        key
                )
        );
    }

    @Test
    void depositShouldSaveSuccessfulTransaction() {

        String key = "DEP-SUCCESS-001";

        BigDecimal amount = new BigDecimal("500");

        BankAccount account = new BankAccount();
        account.setBalance(BigDecimal.ZERO);

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

        depositService.deposit(
                "570929879993",
                "test@gmail.com",
                amount,
                key
        );

        org.mockito.Mockito.verify(bankAccountRepository)
                .save(account);

        org.mockito.Mockito.verify(transactionRepository)
                .save(org.mockito.ArgumentMatchers.any(Transaction.class));

        org.mockito.Mockito.verify(ledgerService).createEntry(
                org.mockito.ArgumentMatchers.any(Transaction.class),
                org.mockito.ArgumentMatchers.eq(account),
                org.mockito.ArgumentMatchers.eq("CREDIT"),
                org.mockito.ArgumentMatchers.eq(amount)
        );
    }
}