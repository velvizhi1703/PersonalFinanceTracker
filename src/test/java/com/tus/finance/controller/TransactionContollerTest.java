package com.tus.finance.controller;

import com.tus.finance.model.Transaction;
import com.tus.finance.model.TransactionType;
import com.tus.finance.model.User;
import com.tus.finance.repository.BudgetRepository;
import com.tus.finance.repository.TransactionRepository;
import com.tus.finance.repository.UserRepository;
import com.tus.finance.security.JwtUtil;
import com.tus.finance.service.TransactionService;
import com.tus.finance.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import java.math.BigDecimal;
import java.util.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TransactionController transactionController;

    private User testUser;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("vel@example.com");

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setUser(testUser);
        testTransaction.setAmount(BigDecimal.valueOf(100.0));
        testTransaction.setType(TransactionType.DEBIT);
        testTransaction.setCategory("Food");
    }

    @Test
    void testGetTransactionById_NotFound() throws Exception {
        when(transactionService.getTransactionById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/transactions/2")
                .with(user("vel@example.com").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTransaction_Success() throws Exception {
        when(transactionService.getTransactionById(1L)).thenReturn(Optional.of(testTransaction));
        Mockito.doNothing().when(transactionService).deleteTransaction(1L);

        mockMvc.perform(delete("/api/transactions/1")
                .with(user("vel@example.com").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteTransaction_NotFound() throws Exception {
        when(transactionService.getTransactionById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/transactions/2")
                .with(user("vel@example.com").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDashboardStats_Success() throws Exception {
        when(jwtUtil.extractUsername("valid-token")).thenReturn("vel@example.com");
        when(userRepository.findByEmail("vel@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.getTotalIncomeForUser(1L)).thenReturn(5000.0);
        when(transactionRepository.getTotalExpenseForUser(1L)).thenReturn(2000.0);
        when(transactionRepository.countByUserId(1L)).thenReturn(10);
        when(transactionRepository.findByUserId(1L)).thenReturn(List.of(testTransaction));

        mockMvc.perform(get("/api/transactions/dashboard")
                .header("Authorization", "Bearer valid-token")
                .with(user("vel@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(5000.0))
                .andExpect(jsonPath("$.expense").value(2000.0))
                .andExpect(jsonPath("$.cash_in_hand").value(3000.0))
                .andExpect(jsonPath("$.num_transactions").value(10));
    }


}
