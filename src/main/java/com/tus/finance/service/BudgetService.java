package com.tus.finance.service;

import com.tus.finance.dto.ApiResponseDto;
import com.tus.finance.dto.BudgetRequest;
import com.tus.finance.model.Budget;
import com.tus.finance.model.User;
import com.tus.finance.repository.BudgetRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;

    @Autowired
    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    /**
     * ✅ Creates a new budget entry
     */
    public ResponseEntity<ApiResponseDto<?>> createBudget(Long userId, BudgetRequest budgetRequest) {
        Budget budget = new Budget();
        budget.setId(userId);
        budget.setMonth(budgetRequest.getMonth());
        budget.setYear(budgetRequest.getYear());
        budget.setAmount(budgetRequest.getAmount());

        budgetRepository.save(budget);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Budget created successfully!", budget));
    }

    /**
     * ✅ Gets a budget for a given user, month, and year
     */
    public ResponseEntity<ApiResponseDto<?>> getBudgetByMonth(Long userId, int month, int year) {
        Optional<Budget> budget = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);

        if (budget.isEmpty()) {
            return ResponseEntity.ok(new ApiResponseDto<>(false, "No budget found for this month & year", null));
        }

        return ResponseEntity.ok(new ApiResponseDto<>(true, "Budget retrieved successfully!", budget.get()));
    }

    /**
     * ✅ Admin fetches all budgets
     */
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }
}
