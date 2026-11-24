
package com.smartfin.smartfin.service;

import com.smartfin.smartfin.dto.TransferRequest;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private TransferService transferService;

    @Test
    void transferShouldRejectSameAccount() {

        TransferRequest request = new TransferRequest();

        request.setSourceAccountNumber("570929879993");
        request.setDestinationAccountNumber("570929879993");

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer("test@gmail.com", request)
        );
    }

    @Test
    void transferShouldRejectInsufficientBalance() {

        BankAccount source = new BankAccount();
        source.setAccountNumber("570929879993");
        source.setBalance(new BigDecimal("100"));

        BankAccount destination = new BankAccount();
        destination.setAccountNumber("412439624695");
        destination.setBalance(new BigDecimal("500"));

        User user = new User();
        user.setEmail("test@gmail.com");
        source.setUser(user);

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("570929879993");
        request.setDestinationAccountNumber("412439624695");
        request.setAmount(new BigDecimal("500"));
        request.setIdempotencyKey("TRF-INSUFFICIENT-001");

        org.mockito.Mockito.when(
                bankAccountRepository.findAccountsForTransferUpdate(
                        org.mockito.ArgumentMatchers.anyList()
                )
        ).thenReturn(List.of(source, destination));

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer("test@gmail.com", request)
        );

        assertEquals(
                0,
                new BigDecimal("100").compareTo(source.getBalance())
        );

        assertEquals(
                0,
                new BigDecimal("500").compareTo(destination.getBalance())
        );
    }

    @Test
    void transferShouldRejectDuplicateTransaction() {

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("570929879993");
        request.setDestinationAccountNumber("412439624695");
        request.setAmount(new BigDecimal("100"));
        request.setIdempotencyKey("TRF-DUPLICATE-001");

        BankAccount source = new BankAccount();
        source.setAccountNumber("570929879993");
        source.setBalance(new BigDecimal("1000"));
        source.setCurrency("INR");
        source.setStatus("ACTIVE");

        BankAccount destination = new BankAccount();
        destination.setAccountNumber("412439624695");
        destination.setBalance(new BigDecimal("500"));
        destination.setCurrency("INR");
        destination.setStatus("ACTIVE");

        User user = new User();
        user.setEmail("test@gmail.com");
        source.setUser(user);

        org.mockito.Mockito.when(
                bankAccountRepository.findAccountsForTransferUpdate(
                        org.mockito.ArgumentMatchers.anyList()
                )
        ).thenReturn(List.of(source, destination));

        org.mockito.Mockito.when(
                transactionRepository.findByIdempotencyKey(
                        "TRF-DUPLICATE-001"
                )
        ).thenReturn(Optional.of(new Transaction()));

        assertThrows(
                DuplicateTransactionException.class,
                () -> transferService.transfer("test@gmail.com", request)
        );
    }

    @Test
    void transferShouldCompleteSuccessfully() {

        BankAccount source = new BankAccount();
        source.setAccountNumber("570929879993");
        source.setBalance(new BigDecimal("1000"));
        source.setCurrency("INR");
        source.setStatus("ACTIVE");

        BankAccount destination = new BankAccount();
        destination.setAccountNumber("412439624695");
        destination.setBalance(new BigDecimal("500"));
        destination.setCurrency("INR");
        destination.setStatus("ACTIVE");

        User user = new User();
        user.setEmail("test@gmail.com");
        source.setUser(user);

        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("570929879993");
        request.setDestinationAccountNumber("412439624695");
        request.setAmount(new BigDecimal("300"));
        request.setIdempotencyKey("TRF-SUCCESS-001");

        org.mockito.Mockito.when(
                bankAccountRepository.findAccountsForTransferUpdate(
                        org.mockito.ArgumentMatchers.anyList()
                )
        ).thenReturn(List.of(source, destination));

        org.mockito.Mockito.when(
                transactionRepository.findByIdempotencyKey("TRF-SUCCESS-001")
        ).thenReturn(Optional.empty());

        org.mockito.Mockito.when(
                userRepository.findByEmail("test@gmail.com")
        ).thenReturn(Optional.of(user));

        org.mockito.Mockito.when(
                transactionRepository.save(
                        org.mockito.ArgumentMatchers.any(Transaction.class)
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transferService.transfer("test@gmail.com", request);

        // Verify sender balance
        assertEquals(
                0,
                new BigDecimal("700").compareTo(source.getBalance())
        );

        // Verify receiver balance
        assertEquals(
                0,
                new BigDecimal("800").compareTo(destination.getBalance())
        );

        // Verify transaction
        assertEquals("TRANSFER", result.getTransactionType());
        assertEquals("SUCCESS", result.getStatus());

        // Verify both accounts saved
        org.mockito.Mockito.verify(bankAccountRepository)
                .save(source);

        org.mockito.Mockito.verify(bankAccountRepository)
                .save(destination);

        // Verify sender DEBIT ledger entry
        org.mockito.Mockito.verify(ledgerService).createEntry(
                org.mockito.ArgumentMatchers.any(Transaction.class),
                org.mockito.ArgumentMatchers.eq(source),
                org.mockito.ArgumentMatchers.eq("DEBIT"),
                org.mockito.ArgumentMatchers.eq(new BigDecimal("300"))
        );

        // Verify receiver CREDIT ledger entry
        org.mockito.Mockito.verify(ledgerService).createEntry(
                org.mockito.ArgumentMatchers.any(Transaction.class),
                org.mockito.ArgumentMatchers.eq(destination),
                org.mockito.ArgumentMatchers.eq("CREDIT"),
                org.mockito.ArgumentMatchers.eq(new BigDecimal("300"))
        );
    }
}
