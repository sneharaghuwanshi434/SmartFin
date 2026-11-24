
package com.smartfin.smartfin.service;

import com.smartfin.smartfin.dto.TransactionResponse;
import com.smartfin.smartfin.entity.Transaction;
import com.smartfin.smartfin.entity.User;
import com.smartfin.smartfin.repository.LedgerEntryRepository;
import com.smartfin.smartfin.repository.TransactionRepository;
import com.smartfin.smartfin.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldRejectNegativePageNumber() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.getUserTransactions(
                        "test@gmail.com", -1, 5, null)
        );

        assertEquals(
                "Invalid pagination parameters",
                exception.getMessage()
        );

        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void shouldRejectZeroPageSize() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.getUserTransactions(
                        "test@gmail.com", 0, 0, null)
        );

        assertEquals(
                "Invalid pagination parameters",
                exception.getMessage()
        );

        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void shouldRejectPageSizeAbove50() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.getUserTransactions(
                        "test@gmail.com", 0, 51, null)
        );

        assertEquals(
                "Invalid pagination parameters",
                exception.getMessage()
        );

        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        when(userRepository.findByEmail("wrong@gmail.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.getUserTransactions(
                        "wrong@gmail.com", 0, 5, null)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("wrong@gmail.com");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldReturnUserTransactionHistory() {

        User user = new User();
        UUID userId = UUID.randomUUID();

        ReflectionTestUtils.setField(user, "id", userId);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        Transaction transaction = new Transaction();
        transaction.setReferenceNumber("TXN-1001");
        transaction.setTransactionType("DEPOSIT");
        transaction.setStatus("SUCCESS");
        transaction.setDescription("Test deposit");

        Page<Transaction> transactionPage = new PageImpl<>(
                List.of(transaction)
        );

        when(transactionRepository
                .findAllByInitiatedByOrderByCreatedAtDesc(
                        eq(userId), any(Pageable.class)))
                .thenReturn(transactionPage);

        Page<TransactionResponse> result =
                transactionService.getUserTransactions(
                        "test@gmail.com", 0, 5, null);

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "TXN-1001",
                result.getContent().get(0).getReferenceNumber()
        );

        assertEquals(
                "DEPOSIT",
                result.getContent().get(0).getTransactionType()
        );

        assertEquals(
                "SUCCESS",
                result.getContent().get(0).getStatus()
        );

        verify(userRepository).findByEmail("test@gmail.com");

        verify(transactionRepository)
                .findAllByInitiatedByOrderByCreatedAtDesc(
                        eq(userId), any(Pageable.class));
    }

    @Test
    void shouldUseCorrectPaginationAndSorting() {

        User user = new User();
        UUID userId = UUID.randomUUID();

        ReflectionTestUtils.setField(user, "id", userId);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(transactionRepository
                .findAllByInitiatedByOrderByCreatedAtDesc(
                        eq(userId), any(Pageable.class)))
                .thenReturn(Page.empty());

        transactionService.getUserTransactions(
                "test@gmail.com", 2, 10, null);

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(transactionRepository)
                .findAllByInitiatedByOrderByCreatedAtDesc(
                        eq(userId), captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        assertTrue(
                pageable.getSort()
                        .getOrderFor("createdAt")
                        .isDescending()
        );
    }
}
