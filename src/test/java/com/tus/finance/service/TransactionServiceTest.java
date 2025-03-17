package com.tus.finance.service;

import com.tus.finance.model.Transaction;
import com.tus.finance.model.User;
import com.tus.finance.repository.TransactionRepository;
import com.tus.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("vel@example.com");

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setUser(testUser);
    }

    @Test
    void testGetTransactionById_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        Optional<Transaction> transaction = transactionService.getTransactionById(1L);

        assertTrue(transaction.isPresent());
        assertEquals(1L, transaction.get().getId());
    }

    @Test
    void testGetTransactionById_NotFound() {
        when(transactionRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Transaction> transaction = transactionService.getTransactionById(2L);

        assertFalse(transaction.isPresent());
    }

    @Test
    void testGetTransactionsByUserId_Success() {
        when(transactionRepository.findByUserId(1L)).thenReturn(List.of(testTransaction));

        List<Transaction> transactions = transactionService.getTransactionsByUserId(1L);

        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());
        assertEquals(1, transactions.size());
        assertEquals(1L, transactions.get(0).getId());
    }

    @Test
    void testGetTransactionsByUserId_EmptyList() {
        when(transactionRepository.findByUserId(1L)).thenReturn(null);

        List<Transaction> transactions = transactionService.getTransactionsByUserId(1L);

        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }

    @Test
    void testAddTransaction_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction savedTransaction = transactionService.addTransaction(testTransaction);

        assertNotNull(savedTransaction);
        assertEquals(1L, savedTransaction.getId());
        assertEquals("vel@example.com", savedTransaction.getUser().getEmail());
    }

    @Test
    void testAddTransaction_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.addTransaction(testTransaction));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testDeleteTransaction_Success() {
        doNothing().when(transactionRepository).deleteById(1L);

        assertDoesNotThrow(() -> transactionService.deleteTransaction(1L));

        verify(transactionRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetAllTransactions_Success() {
        when(transactionRepository.findAll()).thenReturn(List.of(testTransaction));

        List<Transaction> transactions = transactionService.getAllTransactions();

        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());
        assertEquals(1, transactions.size());
    }

   
}
