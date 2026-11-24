
package com.smartfin.smartfin.service;

import com.smartfin.smartfin.dto.AccountStatementResponse;
import com.smartfin.smartfin.entity.BankAccount;
import com.smartfin.smartfin.entity.LedgerEntry;
import com.smartfin.smartfin.entity.Transaction;
import com.smartfin.smartfin.exception.ResourceNotFoundException;
import com.smartfin.smartfin.repository.BankAccountRepository;
import com.smartfin.smartfin.repository.LedgerEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountStatementServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private AccountStatementService accountStatementService;


    @Test
    void shouldThrowExceptionWhenAccountNotFound() {

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountStatementService.getAccountStatement(
                        "1234567890",
                        "test@gmail.com"
                )
        );

        assertEquals("Account not found", exception.getMessage());
    }


    @Test
    void shouldReturnEmptyStatementWhenNoLedgerEntries() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("500.00"));

        UUID accountId = UUID.randomUUID();
        ReflectionTestUtils.setField(account, "id", accountId);

        when(bankAccountRepository
                .findByAccountNumberAndUser_Email(
                        "123456789012", "test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(ledgerEntryRepository
                .findAllByAccount_IdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of());

        List<AccountStatementResponse> result =
                accountStatementService.getAccountStatement(
                        "123456789012", "test@gmail.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(ledgerEntryRepository)
                .findAllByAccount_IdOrderByCreatedAtDesc(accountId);
    }

    @Test
    void shouldCalculateBalanceForCreditEntry() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("600.00"));

        UUID accountId = UUID.randomUUID();
        ReflectionTestUtils.setField(account, "id", accountId);

        Transaction transaction = new Transaction();
        transaction.setReferenceNumber("DEP-1001");
        transaction.setTransactionType("DEPOSIT");

        LedgerEntry entry = new LedgerEntry();
        entry.setTransaction(transaction);
        entry.setEntryType("CREDIT");
        entry.setAmount(new BigDecimal("100.00"));

        when(bankAccountRepository
                .findByAccountNumberAndUser_Email(
                        "123456789012", "test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(ledgerEntryRepository
                .findAllByAccount_IdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of(entry));

        List<AccountStatementResponse> result =
                accountStatementService.getAccountStatement(
                        "123456789012", "test@gmail.com");

        assertEquals(1, result.size());

        assertEquals(
                new BigDecimal("500.00"),
                result.get(0).getBalance()
        );
    }

    @Test
    void shouldCalculateBalanceForDebitEntry() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("400.00"));

        UUID accountId = UUID.randomUUID();
        ReflectionTestUtils.setField(account, "id", accountId);

        Transaction transaction = new Transaction();
        transaction.setReferenceNumber("WTH-1001");
        transaction.setTransactionType("WITHDRAWAL");

        LedgerEntry entry = new LedgerEntry();
        entry.setTransaction(transaction);
        entry.setEntryType("DEBIT");
        entry.setAmount(new BigDecimal("100.00"));

        when(bankAccountRepository
                .findByAccountNumberAndUser_Email(
                        "123456789012", "test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(ledgerEntryRepository
                .findAllByAccount_IdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of(entry));

        List<AccountStatementResponse> result =
                accountStatementService.getAccountStatement(
                        "123456789012", "test@gmail.com");

        assertEquals(1, result.size());

        assertEquals(
                new BigDecimal("500.00"),
                result.get(0).getBalance()
        );
    }


    @Test
    void shouldCalculateBalanceForMixedEntries() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("550.00"));

        UUID accountId = UUID.randomUUID();
        ReflectionTestUtils.setField(account, "id", accountId);

        Transaction debitTransaction = new Transaction();
        debitTransaction.setReferenceNumber("WTH-1001");
        debitTransaction.setTransactionType("WITHDRAWAL");

        Transaction creditTransaction = new Transaction();
        creditTransaction.setReferenceNumber("DEP-1001");
        creditTransaction.setTransactionType("DEPOSIT");

        LedgerEntry debit = new LedgerEntry();
        debit.setTransaction(debitTransaction);
        debit.setEntryType("DEBIT");
        debit.setAmount(new BigDecimal("50.00"));

        LedgerEntry credit = new LedgerEntry();
        credit.setTransaction(creditTransaction);
        credit.setEntryType("CREDIT");
        credit.setAmount(new BigDecimal("100.00"));

        when(bankAccountRepository
                .findByAccountNumberAndUser_Email(
                        "123456789012", "test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(ledgerEntryRepository
                .findAllByAccount_IdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of(debit, credit));

        List<AccountStatementResponse> result =
                accountStatementService.getAccountStatement(
                        "123456789012", "test@gmail.com");

        assertEquals(2, result.size());

        assertEquals(
                new BigDecimal("600.00"),
                result.get(0).getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                result.get(1).getBalance()
        );
    }

}
