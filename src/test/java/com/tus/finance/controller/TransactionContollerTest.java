package com.tus.finance.controller;


import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tus.finance.exception.GlobalExceptionHandler;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import java.math.BigDecimal;
import java.util.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.security.core.Authentication;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
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
    private Authentication authentication;
    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TransactionController transactionController;

    private User testUser;
    private Transaction testTransaction;
    private static final String USER_EMAIL = "vel@example.com";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";

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
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(exceptionHandler)
                .build();
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
    @Test
    void addTransaction_WhenUserNotFound_ShouldReturnNotFound() {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
        
        // Act
        ResponseEntity<EntityModel<Transaction>> response = 
            transactionController.addTransaction(testTransaction, authentication);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    void addTransaction_WithNullTransaction_ShouldReturnBadRequest() {
      
        
        // Act
        ResponseEntity<EntityModel<Transaction>> response = 
            transactionController.addTransaction(null, authentication);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(authentication, never()).getName();
    }
    
    @Test
    void addTransaction_WithValidInput_ShouldReturnCreatedTransaction() {
        // Arrange
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(1L);
        savedTransaction.setUser(testUser);
        when(transactionService.addTransaction(any(Transaction.class))).thenReturn(savedTransaction);
        
        // Act
        ResponseEntity<EntityModel<Transaction>> response = 
            transactionController.addTransaction(testTransaction, authentication);
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getContent().getId());
        assertEquals(testUser, response.getBody().getContent().getUser());
        
        // Verify links
        assertTrue(response.getBody().getLinks().hasSize(2));
        assertTrue(response.getBody().getLinks().stream()
            .anyMatch(link -> link.getRel().value().equals("self")));
        assertTrue(response.getBody().getLinks().stream()
            .anyMatch(link -> link.getRel().value().equals("user-transactions")));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getTransactionsByUserId_ShouldReturnNotFound_WhenNoTransactions() throws Exception {
     
        mockMvc.perform(get("/transactions/user/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDashboardStats_shouldHandleNoTransactions() throws Exception {
        when(jwtUtil.extractUsername("valid-token")).thenReturn("vel@example.com");
        when(userRepository.findByEmail("vel@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.getTotalIncomeForUser(1L)).thenReturn(null);
        when(transactionRepository.getTotalExpenseForUser(1L)).thenReturn(null);
        when(transactionRepository.countByUserId(1L)).thenReturn(0);
        when(transactionRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/transactions/dashboard")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(0.0))
                .andExpect(jsonPath("$.expense").value(0.0))
                .andExpect(jsonPath("$.cash_in_hand").value(0.0))
                .andExpect(jsonPath("$.num_transactions").value(0));
    }

    @Test
    void getDashboardStats_shouldHandleInvalidToken() throws Exception {
        when(jwtUtil.extractUsername("invalid-token"))
            .thenThrow(new RuntimeException("Invalid token"));
        
        mockMvc.perform(get("/api/transactions/dashboard")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid authentication token"));
    }
}
 

