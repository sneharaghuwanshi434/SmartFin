package com.smartfin.smartfin.repository;

import com.smartfin.smartfin.entity.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    List<BankAccount> findAllByUser_Email(String email);

    boolean existsByAccountNumber(String accountNumber);

    Optional<BankAccount> findByAccountNumberAndUser_Email(
            String accountNumber,
            String email
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT a FROM BankAccount a
       WHERE a.accountNumber = :accountNumber
       AND a.user.email = :email
       """)
    Optional<BankAccount> findByAccountNumberAndUserEmailForUpdate(
            @Param("accountNumber") String accountNumber,
            @Param("email") String email
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT a FROM BankAccount a
       WHERE a.accountNumber IN :accountNumbers
       ORDER BY a.accountNumber
       """)
    List<BankAccount> findAccountsForTransferUpdate(
            @Param("accountNumbers") List<String> accountNumbers
    );
}