package com.tus.finance.controller;

import com.tus.finance.dto.ApiResponseDto;
import com.tus.finance.dto.BudgetRequest;
import com.tus.finance.model.Budget;
import com.tus.finance.model.User;
import com.tus.finance.service.BudgetService;
import com.tus.finance.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetControllerTest {

    @Mock
    private BudgetService budgetService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BudgetController budgetController;

    private User mockUser;
    private BudgetRequest budgetRequest;
    private Budget mockBudget;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        budgetRequest = new BudgetRequest();

        mockBudget = new Budget();
        mockBudget.setId(1L);
    }

    @Test
    void createBudget_Success() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(budgetService.createBudget(any(Long.class), any(BudgetRequest.class)))
                .thenReturn(ResponseEntity.ok(new ApiResponseDto<>(true, "Budget created", null)));

        ResponseEntity<ApiResponseDto<?>> response = budgetController.createBudget(budgetRequest, authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void createBudget_UserNotFound() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());

        ResponseEntity<ApiResponseDto<?>> response = budgetController.createBudget(budgetRequest, authentication);

        assertEquals(401, response.getStatusCodeValue());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found", response.getBody().getMessage());
    }

//    @Test
//    void getBudgetByMonth_Success() {
//        when(authentication.getName()).thenReturn("test@example.com");
//        when(userService.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
//        when(budgetService.getBudgetByMonth(anyInt(), anyInt(), anyInt()))
//                .thenReturn(Optional.of(mockBudget));
//
//        ResponseEntity<?> response = budgetController.getBudgetByMonth(3, 2025, authentication);
//
//        assertEquals(200, response.getStatusCodeValue());
//        assertEquals(mockBudget, response.getBody());
//    }
//
//    @Test
//    void getBudgetByMonth_NotFound() {
//        when(authentication.getName()).thenReturn("test@example.com");
//        when(userService.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
//        when(budgetService.getBudgetByMonth(anyInt(), anyInt(), anyInt()))
//                .thenReturn(Optional.empty());
//
//        ResponseEntity<?> response = budgetController.getBudgetByMonth(3, 2025, authentication);
//
//        assertEquals(404, response.getStatusCodeValue());
//    }

    @Test
    void getAllBudgets_Success() {
        when(budgetService.getAllBudgets()).thenReturn(List.of(mockBudget));

        ResponseEntity<List<Budget>> response = budgetController.getAllBudgets();

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }
}