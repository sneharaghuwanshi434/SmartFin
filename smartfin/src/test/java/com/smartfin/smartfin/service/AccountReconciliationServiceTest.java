
package com.smartfin.smartfin.service;

import com.smartfin.smartfin.dto.ReconciliationResponse;
import com.smartfin.smartfin.entity.BankAccount;
import com.smartfin.smartfin.entity.LedgerEntry;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountReconciliationServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private AccountReconciliationService reconciliationService;



    @Test
    void shouldThrowExceptionWhenAccountNotFound() {

        when(bankAccountRepository
                .findByAccountNumberAndUser_Email(
                        "123456789012", "wrong@gmail.com"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reconciliationService.reconcileAccount(
                        "123456789012", "wrong@gmail.com")
        );

        assertEquals("Account not found", exception.getMessage());

        verify(bankAccountRepository)
                .findByAccountNumberAndUser_Email(
                        "123456789012", "wrong@gmail.com");

        verifyNoInteractions(ledgerEntryRepository);
    }

    @Test
    void shouldReconcileAccountWithEmptyLedger() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("500.00"));
        account.setOpeningBalance(new BigDecimal("500.00"));
        account.setAccountNumber("123456789012");

        UUID accountId = UUID.randomUUID();
        ReflectionTestUtils.setField(account, "id", accountId);

        when(bankAccountRepository
                .findByAccountNumberAndUser_Email(
                        "123456789012", "test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(ledgerEntryRepository
                .findAllByAccount_IdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of());

        ReconciliationResponse result =
                reconciliationService.reconcileAccount(
                        "123456789012", "test@gmail.com");

        assertEquals(new BigDecimal("500.00"), result.getCurrentBalance());
        assertEquals(new BigDecimal("500.00"), result.getOpeningBalance());
        assertEquals(BigDecimal.ZERO, result.getTotalCredits());
        assertEquals(BigDecimal.ZERO, result.getTotalDebits());
        assertEquals(new BigDecimal("500.00"), result.getCalculatedBalance());
        assertTrue(result.isReconciled());

    }

    @Test
    void shouldCalculateTotalCreditsCorrectly() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("600.00"));
        account.setOpeningBalance(new BigDecimal("450.00"));
        account.setAccountNumber("123456789012");

        UUID accountId = UUID.randomUUID();
        ReflectionTestUtils.setField(account, "id", accountId);

        LedgerEntry credit1 = new LedgerEntry();
        credit1.setEntryType("CREDIT");
        credit1.setAmount(new BigDecimal("100.00"));

        LedgerEntry credit2 = new LedgerEntry();
        credit2.setEntryType("CREDIT");
        credit2.setAmount(new BigDecimal("50.00"));

        when(bankAccountRepository
                .findByAccountNumberAndUser_Email(
                        "123456789012", "test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(ledgerEntryRepository
                .findAllByAccount_IdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of(credit1, credit2));

        ReconciliationResponse result =
                reconciliationService.reconcileAccount(
                        "123456789012", "test@gmail.com");

        assertEquals(new BigDecimal("150.00"), result.getTotalCredits());
        assertEquals(BigDecimal.ZERO, result.getTotalDebits());
        assertEquals(new BigDecimal("450.00"), result.getOpeningBalance());
        assertTrue(result.isReconciled());

    }

    @Test
    void shouldCalculateTotalDebitsCorrectly() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("400.00"));
        account.setOpeningBalance(new BigDecimal("480.00"));
        account.setAccountNumber("123456789012");

        UUID accountId = UUID.randomUUID();
        ReflectionTestUtils.setField(account, "id", accountId);

        LedgerEntry debit1 = new LedgerEntry();
        debit1.setEntryType("DEBIT");
        debit1.setAmount(new BigDecimal("50.00"));

        LedgerEntry debit2 = new LedgerEntry();
        debit2.setEntryType("DEBIT");
        debit2.setAmount(new BigDecimal("30.00"));

        when(bankAccountRepository
                .findByAccountNumberAndUser_Email(
                        "123456789012", "test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(ledgerEntryRepository
                .findAllByAccount_IdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of(debit1, debit2));

        ReconciliationResponse result =
                reconciliationService.reconcileAccount(
                        "123456789012", "test@gmail.com");

        assertEquals(BigDecimal.ZERO, result.getTotalCredits());
        assertEquals(new BigDecimal("80.00"), result.getTotalDebits());
        assertEquals(new BigDecimal("480.00"), result.getOpeningBalance());
        assertTrue(result.isReconciled());

    }

    @Test
    void shouldReconcileMixedCreditAndDebitEntries() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("550.00"));
        account.setOpeningBalance(new BigDecimal("500.00"));
        account.setAccountNumber("123456789012");

        UUID accountId = UUID.randomUUID();
        ReflectionTestUtils.setField(account, "id", accountId);

        LedgerEntry credit = new LedgerEntry();
        credit.setEntryType("CREDIT");
        credit.setAmount(new BigDecimal("100.00"));

        LedgerEntry debit = new LedgerEntry();
        debit.setEntryType("DEBIT");
        debit.setAmount(new BigDecimal("50.00"));

        when(bankAccountRepository
                .findByAccountNumberAndUser_Email(
                        "123456789012", "test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(ledgerEntryRepository
                .findAllByAccount_IdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of(debit, credit));

        ReconciliationResponse result =
                reconciliationService.reconcileAccount(
                        "123456789012", "test@gmail.com");

        assertEquals(new BigDecimal("100.00"), result.getTotalCredits());
        assertEquals(new BigDecimal("50.00"), result.getTotalDebits());
        assertEquals(new BigDecimal("500.00"), result.getOpeningBalance());
        assertEquals(new BigDecimal("550.00"), result.getCalculatedBalance());
        assertTrue(result.isReconciled());

    }
    @Test
    void shouldDetectReconciliationMismatch() {

        BankAccount account = new BankAccount();
        account.setBalance(new BigDecimal("540.00"));
        account.setOpeningBalance(new BigDecimal("500.00"));
        account.setAccountNumber("123456789012");

        UUID accountId = UUID.randomUUID();
        ReflectionTestUtils.setField(account, "id", accountId);

        LedgerEntry credit = new LedgerEntry();
        credit.setEntryType("CREDIT");
        credit.setAmount(new BigDecimal("100.00"));

        LedgerEntry debit = new LedgerEntry();
        debit.setEntryType("DEBIT");
        debit.setAmount(new BigDecimal("50.00"));

        when(bankAccountRepository
                .findByAccountNumberAndUser_Email(
                        "123456789012", "test@gmail.com"))
                .thenReturn(Optional.of(account));

        when(ledgerEntryRepository
                .findAllByAccount_IdOrderByCreatedAtDesc(accountId))
                .thenReturn(List.of(credit, debit));

        ReconciliationResponse result =
                reconciliationService.reconcileAccount(
                        "123456789012", "test@gmail.com");

        assertEquals(new BigDecimal("500.00"), result.getOpeningBalance());
        assertEquals(new BigDecimal("550.00"), result.getCalculatedBalance());
        assertEquals(new BigDecimal("540.00"), result.getCurrentBalance());

        assertFalse(result.isReconciled());
    }
}
