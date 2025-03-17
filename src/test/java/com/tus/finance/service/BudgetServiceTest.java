package com.tus.finance.service;

import com.tus.finance.dto.ApiResponseDto;
import com.tus.finance.dto.BudgetRequest;
import com.tus.finance.model.Budget;
import com.tus.finance.model.User;
import com.tus.finance.repository.BudgetRepository;
import com.tus.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Budget testBudget;
    private BudgetRequest testBudgetRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("vel@example.com");

        testBudget = new Budget();
        testBudget.setUser(testUser);
        testBudget.setAmount(1000.0);
        testBudget.setSpent(0);
        testBudget.setRemaining(1000.0);
        testBudget.setMonth(3);
        testBudget.setYear(2025);

        testBudgetRequest = new BudgetRequest();
        testBudgetRequest.setAmount(1000.0);
        testBudgetRequest.setMonth(3);
        testBudgetRequest.setYear(2025);
    }

    @Test
    void testCreateBudget_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 3, 2025)).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        ResponseEntity<ApiResponseDto<?>> response = budgetService.createBudget(1L, testBudgetRequest);

        assertNotNull(response);
        assertEquals(201, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Budget created successfully", response.getBody().getMessage());
    }

    @Test
    void testCreateBudget_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponseDto<?>> response = budgetService.createBudget(1L, testBudgetRequest);

        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void testCreateBudget_AlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 3, 2025)).thenReturn(Optional.of(testBudget));

        ResponseEntity<ApiResponseDto<?>> response = budgetService.createBudget(1L, testBudgetRequest);

        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Budget already exists", response.getBody().getMessage());
    }

    @Test
    void testGetBudgetByMonth_Success() {
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 3, 2025)).thenReturn(Optional.of(testBudget));

        ResponseEntity<ApiResponseDto<?>> response = budgetService.getBudgetByMonth(1L, 3, 2025);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Budget retrieved successfully!", response.getBody().getMessage());
    }

    @Test
    void testGetBudgetByMonth_NotFound() {
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 3, 2025)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponseDto<?>> response = budgetService.getBudgetByMonth(1L, 3, 2025);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("No budget found for this month & year", response.getBody().getMessage());
    }

    @Test
    void testGetAllBudgets_Success() {
        when(budgetRepository.findAll()).thenReturn(List.of(testBudget));

        List<Budget> budgets = budgetService.getAllBudgets();

        assertNotNull(budgets);
        assertFalse(budgets.isEmpty());
        assertEquals(1, budgets.size());
    }

    @Test
    void testGetBudgetByMonthAndYear_Success() {
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 3, 2025)).thenReturn(Optional.of(testBudget));

        Optional<Budget> budget = budgetService.getBudgetByMonthAndYear(1L, 3, 2025);

        assertTrue(budget.isPresent());
        assertEquals(1000.0, budget.get().getAmount());
    }

    @Test
    void testGetBudgetByMonthAndYear_NotFound() {
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 3, 2025)).thenReturn(Optional.empty());

        Optional<Budget> budget = budgetService.getBudgetByMonthAndYear(1L, 3, 2025);

        assertFalse(budget.isPresent());
    }

    @Test
    void testGetCurrentMonthBudget_Success() {
        when(budgetRepository.findCurrentMonthBudget(1L)).thenReturn(Optional.of(testBudget));

        Optional<Budget> budget = budgetService.getCurrentMonthBudget(1L);

        assertTrue(budget.isPresent());
        assertEquals(1000.0, budget.get().getAmount());
    }

    @Test
    void testGetCurrentMonthBudget_NotFound() {
        when(budgetRepository.findCurrentMonthBudget(1L)).thenReturn(Optional.empty());

        Optional<Budget> budget = budgetService.getCurrentMonthBudget(1L);

        assertFalse(budget.isPresent());
    }
}
