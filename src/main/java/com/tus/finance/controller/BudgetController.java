package com.tus.finance.controller;

import com.tus.finance.dto.ApiResponseDto;
import com.tus.finance.dto.BudgetRequest;
import com.tus.finance.model.Budget;
import com.tus.finance.model.User;
import com.tus.finance.service.BudgetService;
import com.tus.finance.service.UserService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {
    private final BudgetService budgetService;
    private final UserService userService;

    @Autowired
    public BudgetController(BudgetService budgetService, UserService userService) {
        this.budgetService = budgetService;
        this.userService = userService;
    }

    /**
     * ✅ Allow users to create a budget for a specific month & year
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponseDto<?>> createBudget(@RequestBody BudgetRequest budgetRequest, Authentication authentication) {
        String userEmail = authentication.getName();
        Optional<User> user = userService.findByEmail(userEmail);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponseDto<>(false, "User not found", null));
        }

        return budgetService.createBudget(user.get().getId(), budgetRequest);
    }

    /**
     * ✅ Users can fetch their budget for a specific month & year
     */
    @GetMapping("/get")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponseDto<?>> getBudgetByMonth(
            @RequestParam("month") int month,
            @RequestParam("year") int year,
            Authentication authentication) {

        String userEmail = authentication.getName();
        Optional<User> user = userService.findByEmail(userEmail);

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponseDto<>(false, "User not found", null));
        }

        return budgetService.getBudgetByMonth(user.get().getId(), month, year);
    }

    /**
     * ✅ Admin can fetch all budgets of all users
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Budget>> getAllBudgets() {
        List<Budget> budgets = budgetService.getAllBudgets();
        return ResponseEntity.ok(budgets);
    }
}
